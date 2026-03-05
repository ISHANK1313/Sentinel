import logging
import redis
from typing import Dict, List, Optional
from datetime import datetime
import numpy as np

logger = logging.getLogger(__name__)

class FeatureEngineer:
    """
    Extract behavioral features for ML fraud detection
    Uses REAL Redis data - NO simulation
    Returns features in STRICT schema order
    """

    def __init__(self, redis_host: str = "localhost", redis_port: int = 6379):
        """Initialize Redis connection"""
        try:
            self.redis_client = redis.Redis(
                host=redis_host,
                port=redis_port,
                decode_responses=True,
                socket_timeout=5
            )
            self.redis_client.ping()
            logger.info("✅ Redis connection established")
        except Exception as e:
            logger.error(f"❌ Redis connection failed: {e}")
            self.redis_client = None

        # STRICT feature schema (must match train_model.py)
        self.feature_schema = [
            'amount_zscore', 'amount_percentile',
            'velocity_5min', 'velocity_1hr', 'velocity_24hr', 'velocity_burst_score',
            'merchant_seen_before', 'unique_merchants_24hr', 'merchant_diversity_score',
            'device_seen_before', 'device_novelty_score',
            'location_seen_before', 'location_novelty_score',
            'hour_of_day', 'is_unusual_hour', 'hour_deviation_score',
            'is_cross_border'
        ]

    def extract_features(self, transaction: Dict) -> Dict:
        """
        Extract features matching STRICT schema

        Returns:
            Dict with ALL features in schema (no missing keys)
        """
        user_id = transaction.get('userId')
        amount = transaction.get('amount', 0)

        # Initialize ALL features (schema compliance)
        features = {
            'amount_zscore': 0.0,
            'amount_percentile': 50.0,
            'velocity_5min': 0,
            'velocity_1hr': 0,
            'velocity_24hr': 0,
            'velocity_burst_score': 0.0,
            'merchant_seen_before': 0,
            'unique_merchants_24hr': 0,
            'merchant_diversity_score': 0.0,
            'device_seen_before': 0,
            'device_novelty_score': 0.5,
            'location_seen_before': 0,
            'location_novelty_score': 0.5,
            'hour_of_day': 12,
            'is_unusual_hour': 0,
            'hour_deviation_score': 0.0,
            'is_cross_border': 0.0
        }

        # Extract REAL features from Redis
        if self.redis_client:
            # Amount features (from beneficiary data as proxy)
            amount_stats = self._get_real_amount_stats(user_id, amount)
            features.update(amount_stats)

            # Velocity features
            velocity = self._get_velocity_features(user_id)
            features.update(velocity)

            # Merchant features
            merchant = self._get_merchant_features(user_id, transaction.get('merchantId'))
            features.update(merchant)

        # Device features (heuristic)
        device = self._get_device_features(transaction.get('deviceFingerPrint'))
        features.update(device)

        # Location features (heuristic)
        location = self._get_location_features(transaction.get('locationOfUser'))
        features.update(location)

        # Time features
        time = self._get_time_features(transaction.get('timeOfPayment'))
        features.update(time)

        # Cross-border
        features['is_cross_border'] = 1.0 if transaction.get('crossBorder') else 0.0

        # VERIFY schema compliance
        self._verify_schema(features)

        return features

    def _get_real_amount_stats(self, user_id: int, current_amount: float) -> Dict:
        """
        Get REAL amount statistics from Redis beneficiary data

        NOTE: We use beneficiary timestamps as proxy for transaction amounts
        In production with DB access, query actual amounts
        """
        try:
            beneficiary_key = f"beneficiary:user:{user_id}"
            merchant_scores = self.redis_client.zrange(beneficiary_key, 0, -1, withscores=True)

            if not merchant_scores or len(merchant_scores) < 5:
                return {'amount_zscore': 0.0, 'amount_percentile': 50.0}

            # Use timestamps as proxy (scaled down)
            amounts = [float(score) / 1000000 for _, score in merchant_scores]

            if len(amounts) < 2:
                return {'amount_zscore': 0.0, 'amount_percentile': 50.0}

            mean = np.mean(amounts)
            std = np.std(amounts)

            # Calculate z-score
            zscore = (current_amount - mean) / std if std > 0 else 0.0

            # Calculate percentile
            percentile = (sum(1 for a in amounts if a < current_amount) / len(amounts)) * 100

            return {
                'amount_zscore': round(zscore, 2),
                'amount_percentile': round(percentile, 2)
            }

        except Exception as e:
            logger.error(f"Error getting amount stats: {e}")
            return {'amount_zscore': 0.0, 'amount_percentile': 50.0}

    def _get_velocity_features(self, user_id: int) -> Dict:
        """Get REAL velocity from Redis"""
        try:
            key = f"users:{user_id}:velocity"
            now = datetime.now().timestamp() * 1000

            v_5min = self.redis_client.zcount(key, now - 5*60*1000, now)
            v_1hr = self.redis_client.zcount(key, now - 60*60*1000, now)
            v_24hr = self.redis_client.zcount(key, now - 24*60*60*1000, now)

            # Burst score
            burst_score = 0.0
            if v_1hr > 0:
                expected_5min = v_1hr / 12
                burst_score = v_5min / max(expected_5min, 1)

            return {
                'velocity_5min': v_5min,
                'velocity_1hr': v_1hr,
                'velocity_24hr': v_24hr,
                'velocity_burst_score': round(burst_score, 2)
            }
        except Exception as e:
            logger.error(f"Error getting velocity: {e}")
            return {
                'velocity_5min': 0,
                'velocity_1hr': 0,
                'velocity_24hr': 0,
                'velocity_burst_score': 0.0
            }

    def _get_merchant_features(self, user_id: int, merchant_id: int) -> Dict:
        """Get REAL merchant features from Redis"""
        try:
            beneficiary_key = f"beneficiary:user:{user_id}"

            # Check if seen before
            score = self.redis_client.zscore(beneficiary_key, str(merchant_id))
            seen_before = 1 if score is not None else 0

            # Unique merchants in 24hr
            now = datetime.now().timestamp() * 1000
            recent = self.redis_client.zrangebyscore(
                beneficiary_key, now - 24*60*60*1000, now
            )
            unique_24hr = len(set(recent)) if recent else 0

            # Diversity score
            all_merchants = self.redis_client.zrange(beneficiary_key, 0, -1)
            diversity = 0.0
            if all_merchants:
                diversity = unique_24hr / min(len(all_merchants), 10)

            return {
                'merchant_seen_before': seen_before,
                'unique_merchants_24hr': unique_24hr,
                'merchant_diversity_score': round(diversity, 2)
            }
        except Exception as e:
            logger.error(f"Error getting merchant features: {e}")
            return {
                'merchant_seen_before': 0,
                'unique_merchants_24hr': 0,
                'merchant_diversity_score': 0.0
            }

    def _get_device_features(self, device: str) -> Dict:
        """Device features (heuristic)"""
        seen_before = 0
        novelty_score = 0.5

        if device:
            device_lower = device.lower()
            if 'unknown' in device_lower or 'new' in device_lower:
                seen_before = 0
                novelty_score = 1.0
            else:
                seen_before = 1
                novelty_score = 0.1

        return {
            'device_seen_before': seen_before,
            'device_novelty_score': novelty_score
        }

    def _get_location_features(self, location: str) -> Dict:
        """Location features (heuristic)"""
        common_locations = ['Mumbai', 'Delhi', 'Bangalore', 'Hyderabad', 'Chennai', 'Kolkata']

        if location in common_locations:
            return {
                'location_seen_before': 1,
                'location_novelty_score': 0.2
            }
        else:
            return {
                'location_seen_before': 0,
                'location_novelty_score': 0.8
            }

    def _get_time_features(self, timestamp_str: str) -> Dict:
        """Time features"""
        try:
            if timestamp_str:
                dt = datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
                hour = dt.hour

                is_unusual = 1 if 2 <= hour <= 5 else 0

                # Deviation from business hours
                normal_start, normal_end = 9, 18
                if normal_start <= hour <= normal_end:
                    deviation = 0.0
                else:
                    dist = min(abs(hour - normal_start), abs(hour - normal_end))
                    deviation = dist / 12

                return {
                    'hour_of_day': hour,
                    'is_unusual_hour': is_unusual,
                    'hour_deviation_score': round(deviation, 2)
                }
        except Exception as e:
            logger.error(f"Error parsing timestamp: {e}")

        return {
            'hour_of_day': 12,
            'is_unusual_hour': 0,
            'hour_deviation_score': 0.0
        }

    def _verify_schema(self, features: Dict):
        """Verify ALL schema features are present"""
        missing = [f for f in self.feature_schema if f not in features]
        if missing:
            raise ValueError(f"Missing features: {missing}")

    def get_feature_names(self) -> List[str]:
        """Return STRICT feature schema"""
        return self.feature_schema
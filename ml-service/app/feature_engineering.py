import logging
import redis
import psycopg2
from typing import Dict, List, Optional
from datetime import datetime, timedelta
import numpy as np

logger = logging.getLogger(__name__)

class FeatureEngineer:
    """
    Extract behavioral features for ML fraud detection
    Focuses on user-specific patterns and statistical deviations
    """

    def __init__(self, redis_host: str = "localhost", redis_port: int = 6379):
        """Initialize Redis connection for velocity/history data"""
        try:
            self.redis_client = redis.Redis(
                host=redis_host,
                port=redis_port,
                decode_responses=True,
                socket_timeout=5
            )
            # Test connection
            self.redis_client.ping()
            logger.info("Redis connection established")
        except Exception as e:
            logger.error(f"Redis connection failed: {e}")
            self.redis_client = None

    def extract_features(self, transaction: Dict) -> Dict:
        """
        Extract behavioral features for ML model

        Returns feature dict with:
        - Statistical deviations (z-scores)
        - Novelty indicators (new merchant, new device, etc.)
        - Velocity patterns
        - Behavioral context
        """
        user_id = transaction.get('userId')
        amount = transaction.get('amount', 0)

        features = {}

        # 1. AMOUNT BEHAVIORAL FEATURES
        amount_stats = self._get_user_amount_statistics(user_id)
        if amount_stats:
            features['amount'] = amount
            features['amount_zscore'] = self._calculate_zscore(
                amount, amount_stats['mean'], amount_stats['std']
            )
            features['amount_percentile'] = self._calculate_percentile(
                amount, amount_stats['amounts']
            )
        else:
            features['amount'] = amount
            features['amount_zscore'] = 0.0
            features['amount_percentile'] = 50.0

        # 2. VELOCITY BEHAVIORAL FEATURES (from Redis)
        velocity_features = self._get_velocity_features(user_id)
        features.update(velocity_features)

        # 3. MERCHANT BEHAVIORAL FEATURES
        merchant_features = self._get_merchant_features(
            user_id, transaction.get('merchantId')
        )
        features.update(merchant_features)

        # 4. DEVICE BEHAVIORAL FEATURES
        device_features = self._get_device_features(
            user_id, transaction.get('deviceFingerPrint')
        )
        features.update(device_features)

        # 5. LOCATION BEHAVIORAL FEATURES
        location_features = self._get_location_features(
            user_id, transaction.get('locationOfUser')
        )
        features.update(location_features)

        # 6. TIME BEHAVIORAL FEATURES
        time_features = self._get_time_features(
            user_id, transaction.get('timeOfPayment')
        )
        features.update(time_features)

        # 7. CROSS-BORDER (simple boolean)
        features['is_cross_border'] = 1.0 if transaction.get('crossBorder') else 0.0

        logger.debug(f"Extracted {len(features)} features for user {user_id}")

        return features

    def _get_user_amount_statistics(self, user_id: int) -> Optional[Dict]:
        """Get user's historical amount statistics from Redis"""
        if not self.redis_client:
            return None

        try:
            # Get user's transaction history from Redis
            history_key = f"history:users:{user_id}"
            txn_ids = self.redis_client.zrange(history_key, 0, -1)

            if not txn_ids or len(txn_ids) < 5:
                return None

            # In production, fetch actual amounts from DB
            # For now, simulate with reasonable values
            # TODO: Replace with actual DB query
            amounts = self._simulate_historical_amounts(len(txn_ids))

            return {
                'mean': np.mean(amounts),
                'std': np.std(amounts) if len(amounts) > 1 else 1.0,
                'amounts': amounts
            }

        except Exception as e:
            logger.error(f"Error getting amount statistics: {e}")
            return None

    def _simulate_historical_amounts(self, count: int) -> List[float]:
        """Simulate historical amounts - replace with DB query in production"""
        # Simulate realistic transaction amounts
        base = 50000
        return [base + np.random.normal(0, 20000) for _ in range(count)]

    def _calculate_zscore(self, value: float, mean: float, std: float) -> float:
        """Calculate z-score (standard deviations from mean)"""
        if std == 0:
            return 0.0
        return round((value - mean) / std, 2)

    def _calculate_percentile(self, value: float, values: List[float]) -> float:
        """Calculate percentile rank (0-100)"""
        if not values:
            return 50.0
        percentile = (sum(1 for v in values if v < value) / len(values)) * 100
        return round(percentile, 2)

    def _get_velocity_features(self, user_id: int) -> Dict:
        """Get transaction velocity from Redis"""
        features = {
            'velocity_5min': 0,
            'velocity_1hr': 0,
            'velocity_24hr': 0,
            'velocity_burst_score': 0.0
        }

        if not self.redis_client:
            return features

        try:
            key = f"users:{user_id}:velocity"
            now = datetime.now().timestamp() * 1000

            # Count transactions in different windows
            features['velocity_5min'] = self.redis_client.zcount(
                key, now - 5*60*1000, now
            )
            features['velocity_1hr'] = self.redis_client.zcount(
                key, now - 60*60*1000, now
            )
            features['velocity_24hr'] = self.redis_client.zcount(
                key, now - 24*60*60*1000, now
            )

            # Calculate burst score (acceleration)
            # High if 5min count is disproportionate to 1hr count
            if features['velocity_1hr'] > 0:
                expected_5min = (features['velocity_1hr'] / 12)  # Expected if uniform
                features['velocity_burst_score'] = round(
                    features['velocity_5min'] / max(expected_5min, 1), 2
                )

        except Exception as e:
            logger.error(f"Error getting velocity features: {e}")

        return features

    def _get_merchant_features(self, user_id: int, merchant_id: int) -> Dict:
        """Get merchant behavioral features"""
        features = {
            'merchant_seen_before': 0,
            'merchant_frequency': 0.0,
            'unique_merchants_24hr': 0,
            'merchant_diversity_score': 0.0
        }

        if not self.redis_client or not merchant_id:
            return features

        try:
            # Check if merchant seen before
            beneficiary_key = f"beneficiary:user:{user_id}"
            score = self.redis_client.zscore(beneficiary_key, str(merchant_id))
            features['merchant_seen_before'] = 1 if score is not None else 0

            # Count unique merchants in 24hr
            now = datetime.now().timestamp() * 1000
            recent_merchants = self.redis_client.zrangebyscore(
                beneficiary_key,
                now - 24*60*60*1000,
                now
            )
            features['unique_merchants_24hr'] = len(set(recent_merchants)) if recent_merchants else 0

            # Merchant diversity score (higher = more spread across merchants)
            all_merchants = self.redis_client.zrange(beneficiary_key, 0, -1)
            if all_merchants and len(all_merchants) > 0:
                features['merchant_diversity_score'] = round(
                    features['unique_merchants_24hr'] / min(len(all_merchants), 10), 2
                )

        except Exception as e:
            logger.error(f"Error getting merchant features: {e}")

        return features

    def _get_device_features(self, user_id: int, device: str) -> Dict:
        """Get device behavioral features"""
        features = {
            'device_seen_before': 0,
            'device_novelty_score': 1.0
        }

        # Simple heuristic: if device contains "unknown" or "new", it's suspicious
        if device:
            device_lower = device.lower()
            if 'unknown' in device_lower or 'new' in device_lower:
                features['device_seen_before'] = 0
                features['device_novelty_score'] = 1.0
            else:
                features['device_seen_before'] = 1
                features['device_novelty_score'] = 0.1

        return features

    def _get_location_features(self, user_id: int, location: str) -> Dict:
        """Get location behavioral features"""
        features = {
            'location_seen_before': 0,
            'location_novelty_score': 0.5
        }

        # Simplified: Check if location is common
        # In production, query historical locations
        common_locations = ['Mumbai', 'Delhi', 'Bangalore', 'Hyderabad']
        if location in common_locations:
            features['location_seen_before'] = 1
            features['location_novelty_score'] = 0.2
        else:
            features['location_seen_before'] = 0
            features['location_novelty_score'] = 0.8

        return features

    def _get_time_features(self, user_id: int, timestamp_str: str) -> Dict:
        """Get time behavioral features"""
        features = {
            'hour_of_day': 12,
            'is_unusual_hour': 0,
            'hour_deviation_score': 0.0
        }

        try:
            if timestamp_str:
                dt = datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
                hour = dt.hour
                features['hour_of_day'] = hour

                # Unusual hours (2 AM - 5 AM)
                features['is_unusual_hour'] = 1 if 2 <= hour <= 5 else 0

                # Calculate deviation from normal business hours (9-18)
                normal_start, normal_end = 9, 18
                if normal_start <= hour <= normal_end:
                    features['hour_deviation_score'] = 0.0
                else:
                    # Distance from nearest normal hour
                    dist_to_normal = min(
                        abs(hour - normal_start),
                        abs(hour - normal_end)
                    )
                    features['hour_deviation_score'] = round(dist_to_normal / 12, 2)

        except Exception as e:
            logger.error(f"Error parsing timestamp: {e}")

        return features

    def get_feature_names(self) -> List[str]:
        """Return list of feature names for ML model"""
        return [
            'amount',
            'amount_zscore',
            'amount_percentile',
            'velocity_5min',
            'velocity_1hr',
            'velocity_24hr',
            'velocity_burst_score',
            'merchant_seen_before',
            'merchant_frequency',
            'unique_merchants_24hr',
            'merchant_diversity_score',
            'device_seen_before',
            'device_novelty_score',
            'location_seen_before',
            'location_novelty_score',
            'hour_of_day',
            'is_unusual_hour',
            'hour_deviation_score',
            'is_cross_border'
        ]
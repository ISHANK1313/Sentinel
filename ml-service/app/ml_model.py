import numpy as np
from typing import Dict, Tuple, List
import logging

logger = logging.getLogger(__name__)

class FraudDetectionModel:
    """
    Behavioral anomaly detection for fraud
    Uses statistical features to detect deviations from normal user behavior
    """

    def __init__(self, model_version: str = "behavioral-v2.0"):
        self.model_version = model_version

        # Feature importance weights (learned from data in production)
        self.feature_weights = {
            'amount_zscore': 0.20,           # How unusual is the amount?
            'velocity_burst_score': 0.15,    # Sudden transaction burst?
            'merchant_novelty': 0.15,        # New merchant?
            'device_novelty': 0.15,          # New device?
            'location_novelty': 0.10,        # New location?
            'hour_deviation': 0.10,          # Unusual time?
            'merchant_diversity': 0.10,      # Spreading to many merchants?
            'is_cross_border': 0.05          # Cross-border transaction?
        }

        logger.info(f"Initialized Behavioral Fraud Detection Model {model_version}")

    def predict(self, features: Dict) -> Tuple[float, float, str]:
        """
        Predict fraud probability based on behavioral features

        Args:
            features: Behavioral feature dict from FeatureEngineer

        Returns:
            Tuple of (fraud_score_percentage, confidence, prediction_class)
        """
        try:
            # Calculate anomaly scores for each feature category
            anomaly_scores = self._calculate_anomaly_scores(features)

            # Weighted combination
            fraud_probability = sum(
                anomaly_scores[category] * weight
                for category, weight in self.feature_weights.items()
            )

            # Clip to [0, 1]
            fraud_probability = np.clip(fraud_probability, 0, 1)

            # Convert to percentage
            fraud_score = round(fraud_probability * 100, 2)

            # Calculate confidence
            confidence = self._calculate_confidence(fraud_probability, features)

            # Classification
            prediction_class = "FRAUD" if fraud_probability > 0.5 else "LEGITIMATE"

            logger.info(
                f"Behavioral prediction: {fraud_score}% ({prediction_class}) "
                f"[confidence: {confidence}]"
            )

            return fraud_score, confidence, prediction_class

        except Exception as e:
            logger.error(f"Prediction error: {str(e)}")
            return 50.0, 0.5, "UNKNOWN"

    def _calculate_anomaly_scores(self, features: Dict) -> Dict[str, float]:
        """Calculate anomaly score for each feature category"""
        scores = {}

        # 1. Amount anomaly (z-score based)
        amount_zscore = features.get('amount_zscore', 0)
        scores['amount_zscore'] = self._zscore_to_anomaly(amount_zscore)

        # 2. Velocity burst anomaly
        burst_score = features.get('velocity_burst_score', 0)
        velocity_5min = features.get('velocity_5min', 0)

        if velocity_5min > 3:
            scores['velocity_burst_score'] = min(burst_score / 3, 1.0)
        else:
            scores['velocity_burst_score'] = 0.0

        # 3. Merchant novelty
        merchant_seen = features.get('merchant_seen_before', 1)
        unique_merchants = features.get('unique_merchants_24hr', 0)

        if merchant_seen == 0:
            scores['merchant_novelty'] = 0.8  # New merchant = high risk
        elif unique_merchants > 10:
            scores['merchant_novelty'] = 0.6  # Too many merchants = suspicious
        else:
            scores['merchant_novelty'] = 0.1

        # 4. Device novelty
        device_seen = features.get('device_seen_before', 1)
        device_novelty = features.get('device_novelty_score', 0)

        scores['device_novelty'] = device_novelty

        # 5. Location novelty
        location_seen = features.get('location_seen_before', 1)
        location_novelty = features.get('location_novelty_score', 0)

        scores['location_novelty'] = location_novelty

        # 6. Time deviation
        is_unusual = features.get('is_unusual_hour', 0)
        hour_deviation = features.get('hour_deviation_score', 0)

        if is_unusual:
            scores['hour_deviation'] = 0.8
        else:
            scores['hour_deviation'] = hour_deviation * 0.5

        # 7. Merchant diversity (spreading pattern)
        diversity = features.get('merchant_diversity_score', 0)
        scores['merchant_diversity'] = diversity if diversity > 0.5 else 0.0

        # 8. Cross-border
        scores['is_cross_border'] = features.get('is_cross_border', 0) * 0.7

        return scores

    def _zscore_to_anomaly(self, zscore: float) -> float:
        """
        Convert z-score to anomaly score (0-1)

        Z-score interpretation:
        0-1: Normal (0-0.3 anomaly)
        1-2: Slightly unusual (0.3-0.6 anomaly)
        2-3: Very unusual (0.6-0.85 anomaly)
        >3: Extreme outlier (0.85-1.0 anomaly)
        """
        abs_z = abs(zscore)

        if abs_z < 1:
            return abs_z * 0.3
        elif abs_z < 2:
            return 0.3 + (abs_z - 1) * 0.3
        elif abs_z < 3:
            return 0.6 + (abs_z - 2) * 0.25
        else:
            return min(0.85 + (abs_z - 3) * 0.05, 1.0)

    def _calculate_confidence(self, probability: float, features: Dict) -> float:
        """
        Calculate model confidence based on prediction and feature quality

        Higher confidence when:
        - Prediction is extreme (very high or very low)
        - More behavioral data available
        """
        # Base confidence from prediction extremity
        distance_from_neutral = abs(probability - 0.5)
        base_confidence = 0.5 + (distance_from_neutral * 0.8)

        # Adjust based on data availability
        data_quality_factors = [
            features.get('velocity_24hr', 0) > 5,  # Has transaction history
            features.get('merchant_seen_before', 0) == 1,  # Known merchant
            features.get('device_seen_before', 0) == 1,    # Known device
        ]

        data_quality = sum(data_quality_factors) / len(data_quality_factors)

        # Combine
        confidence = base_confidence * 0.7 + data_quality * 0.3

        return round(confidence, 2)

    def get_model_info(self) -> Dict:
        """Return model metadata"""
        return {
            "model_version": self.model_version,
            "model_type": "Behavioral Anomaly Detection",
            "approach": "Statistical deviation from user baseline",
            "features": list(self.feature_weights.keys()),
            "weights": self.feature_weights,
            "differentiator": "Detects behavioral anomalies that rules cannot"
        }
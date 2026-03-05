import numpy as np
from typing import Dict, Tuple, List
import logging
import joblib
import json
from pathlib import Path

logger = logging.getLogger(__name__)

class FraudDetectionModel:
    """
    REAL ML-based fraud detection using Isolation Forest
    Loads trained model from disk - NOT weighted rules
    """

    def __init__(self, model_path: str = "models/fraud_model.pkl"):
        self.model_path = model_path
        self.scaler_path = "models/scaler.pkl"
        self.schema_path = "models/feature_schema.json"

        # Load trained model
        try:
            self.model = joblib.load(self.model_path)
            self.scaler = joblib.load(self.scaler_path)

            with open(self.schema_path, 'r') as f:
                schema = json.load(f)
                self.feature_names = schema['features']

            logger.info(f"✅ Loaded Isolation Forest model from {self.model_path}")
            logger.info(f"✅ Feature schema: {len(self.feature_names)} features")
            logger.info(f"✅ Model ready for predictions")

        except FileNotFoundError as e:
            logger.error(f"❌ Model files not found! Run train_model.py first")
            logger.error(f"   Missing: {e.filename}")
            raise RuntimeError("Model not trained. Run: python train_model.py")
        except Exception as e:
            logger.error(f"❌ Error loading model: {e}")
            raise

    def predict(self, features: Dict) -> Tuple[float, float, str]:
        """
        Predict fraud using REAL ML model (Isolation Forest)

        Args:
            features: Feature dict matching STRICT schema

        Returns:
            (fraud_score, confidence, prediction_class)
        """
        try:
            # STRICT feature schema enforcement
            feature_vector = self._extract_feature_vector(features)

            # Scale features
            feature_vector_scaled = self.scaler.transform([feature_vector])

            # Predict using Isolation Forest
            # Returns -1 for anomalies, 1 for normal
            prediction = self.model.predict(feature_vector_scaled)[0]

            # Get anomaly score (lower = more anomalous)
            anomaly_score = self.model.score_samples(feature_vector_scaled)[0]

            # Convert to fraud probability (0-100%)
            fraud_probability = self._anomaly_score_to_probability(anomaly_score)
            fraud_score = round(fraud_probability * 100, 2)

            # Calculate confidence
            confidence = self._calculate_confidence(anomaly_score)

            # Classification
            prediction_class = "FRAUD" if prediction == -1 else "LEGITIMATE"

            logger.info(
                f"ML Prediction: {fraud_score}% ({prediction_class}) "
                f"[anomaly_score: {anomaly_score:.3f}, conf: {confidence}]"
            )

            return fraud_score, confidence, prediction_class

        except Exception as e:
            logger.error(f"Prediction error: {str(e)}")
            return 50.0, 0.5, "UNKNOWN"

    def _extract_feature_vector(self, features: Dict) -> List[float]:
        """
        Extract feature vector in STRICT schema order
        Raises error if features don't match schema
        """
        try:
            return [float(features[name]) for name in self.feature_names]
        except KeyError as e:
            missing_feature = str(e).strip("'")
            logger.error(f"❌ Missing feature: {missing_feature}")
            logger.error(f"   Expected: {self.feature_names}")
            logger.error(f"   Got: {list(features.keys())}")
            raise ValueError(f"Feature schema mismatch: missing {missing_feature}")

    def _anomaly_score_to_probability(self, anomaly_score: float) -> float:
        """
        Convert Isolation Forest anomaly score to fraud probability

        Isolation Forest scores typically range from -0.5 to 0.5
        More negative = more anomalous = higher fraud probability
        """
        # Sigmoid transformation
        normalized = -anomaly_score
        probability = 1 / (1 + np.exp(-5 * normalized))

        return np.clip(probability, 0, 1)

    def _calculate_confidence(self, anomaly_score: float) -> float:
        """
        Calculate confidence based on anomaly score magnitude
        More extreme scores = higher confidence
        """
        distance = abs(anomaly_score)
        confidence = 0.5 + min(distance * 2, 0.45)

        return round(confidence, 2)

    def get_model_info(self) -> Dict:
        """Return model metadata"""
        return {
            "model_type": "Isolation Forest",
            "approach": "Unsupervised anomaly detection",
            "features": self.feature_names,
            "n_features": len(self.feature_names),
            "model_file": self.model_path,
            "is_trained": True,
            "loaded_successfully": True
        }
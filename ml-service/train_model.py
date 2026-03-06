"""
Train fraud detection model using Isolation Forest
Run this script to generate fraud_model.pkl
"""

import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import joblib
import json
import os

def generate_synthetic_training_data(n_samples=10000):
    """
    Generate synthetic training data for fraud detection
    In production, replace with real historical data from database
    """
    np.random.seed(42)

    # Legitimate transactions (90%)
    n_legit = int(n_samples * 0.9)
    legit_data = {
        'amount_zscore': np.random.normal(0, 1, n_legit),
        'amount_percentile': np.random.uniform(20, 80, n_legit),
        'velocity_5min': np.random.poisson(1, n_legit),
        'velocity_1hr': np.random.poisson(5, n_legit),
        'velocity_24hr': np.random.poisson(20, n_legit),
        'velocity_burst_score': np.random.uniform(0, 1.5, n_legit),
        'merchant_seen_before': np.random.choice([0, 1], n_legit, p=[0.2, 0.8]),
        'unique_merchants_24hr': np.random.poisson(3, n_legit),
        'merchant_diversity_score': np.random.uniform(0, 0.5, n_legit),
        'device_seen_before': np.random.choice([0, 1], n_legit, p=[0.1, 0.9]),
        'device_novelty_score': np.random.uniform(0, 0.3, n_legit),
        'location_seen_before': np.random.choice([0, 1], n_legit, p=[0.15, 0.85]),
        'location_novelty_score': np.random.uniform(0, 0.4, n_legit),
        'hour_of_day': np.random.choice(range(8, 20), n_legit),
        'is_unusual_hour': np.zeros(n_legit),
        'hour_deviation_score': np.random.uniform(0, 0.2, n_legit),
        'is_cross_border': np.random.choice([0, 1], n_legit, p=[0.9, 0.1]),
    }

    # Fraudulent transactions (10%)
    n_fraud = n_samples - n_legit
    fraud_data = {
        'amount_zscore': np.random.normal(3, 1.5, n_fraud),
        'amount_percentile': np.random.uniform(80, 99, n_fraud),
        'velocity_5min': np.random.poisson(4, n_fraud),
        'velocity_1hr': np.random.poisson(15, n_fraud),
        'velocity_24hr': np.random.poisson(50, n_fraud),
        'velocity_burst_score': np.random.uniform(2, 5, n_fraud),
        'merchant_seen_before': np.random.choice([0, 1], n_fraud, p=[0.7, 0.3]),
        'unique_merchants_24hr': np.random.poisson(12, n_fraud),
        'merchant_diversity_score': np.random.uniform(0.6, 1.0, n_fraud),
        'device_seen_before': np.random.choice([0, 1], n_fraud, p=[0.6, 0.4]),
        'device_novelty_score': np.random.uniform(0.7, 1.0, n_fraud),
        'location_seen_before': np.random.choice([0, 1], n_fraud, p=[0.5, 0.5]),
        'location_novelty_score': np.random.uniform(0.6, 1.0, n_fraud),
        'hour_of_day': np.random.choice(range(0, 6), n_fraud),
        'is_unusual_hour': np.ones(n_fraud),
        'hour_deviation_score': np.random.uniform(0.6, 1.0, n_fraud),
        'is_cross_border': np.random.choice([0, 1], n_fraud, p=[0.3, 0.7]),
    }

    # Combine
    df_legit = pd.DataFrame(legit_data)
    df_legit['is_fraud'] = 0

    df_fraud = pd.DataFrame(fraud_data)
    df_fraud['is_fraud'] = 1

    df = pd.concat([df_legit, df_fraud], ignore_index=True)
    df = df.sample(frac=1, random_state=42).reset_index(drop=True)

    return df

def train_model():
    """Train Isolation Forest model"""
    print("=" * 60)
    print("FRAUD DETECTION MODEL TRAINING")
    print("=" * 60)

    print("\n[1/5] Generating training data...")
    df = generate_synthetic_training_data(n_samples=10000)

    # Feature columns (STRICT SCHEMA)
    feature_cols = [
        'amount_zscore', 'amount_percentile',
        'velocity_5min', 'velocity_1hr', 'velocity_24hr', 'velocity_burst_score',
        'merchant_seen_before', 'unique_merchants_24hr', 'merchant_diversity_score',
        'device_seen_before', 'device_novelty_score',
        'location_seen_before', 'location_novelty_score',
        'hour_of_day', 'is_unusual_hour', 'hour_deviation_score',
        'is_cross_border'
    ]

    X = df[feature_cols]
    y = df['is_fraud']

    print(f"   ✓ Generated {len(X)} samples")
    print(f"   ✓ Fraud samples: {y.sum()} ({y.sum()/len(y)*100:.1f}%)")
    print(f"   ✓ Legitimate samples: {len(y) - y.sum()} ({(len(y)-y.sum())/len(y)*100:.1f}%)")

    print("\n[2/5] Scaling features...")
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    print(f"   ✓ Features scaled using StandardScaler")

    print("\n[3/5] Training Isolation Forest...")
    model = IsolationForest(
        n_estimators=100,
        contamination=0.1,
        max_samples='auto',
        random_state=42,
        n_jobs=-1
    )
    model.fit(X_scaled)
    print(f"   ✓ Trained with {model.n_estimators} trees")

    print("\n[4/5] Saving model artifacts...")

    # Create models directory
    os.makedirs('models', exist_ok=True)

    # Save model
    joblib.dump(model, 'models/fraud_model.pkl')
    print(f"   ✓ Model saved: models/fraud_model.pkl")

    # Save scaler
    joblib.dump(scaler, 'models/scaler.pkl')
    print(f"   ✓ Scaler saved: models/scaler.pkl")

    # Save feature schema
    schema = {
        'features': feature_cols,
        'n_features': len(feature_cols),
        'model_type': 'IsolationForest',
        'contamination': 0.1,
        'n_estimators': 100
    }
    with open('models/feature_schema.json', 'w') as f:
        json.dump(schema, f, indent=2)
    print(f"   ✓ Schema saved: models/feature_schema.json")

    print("\n[5/5] Validating model...")
    predictions = model.predict(X_scaled)
    anomalies = (predictions == -1).sum()
    print(f"   ✓ Anomalies detected: {anomalies}/{len(predictions)} ({anomalies/len(predictions)*100:.1f}%)")

    print("\n" + "=" * 60)
    print("✅ MODEL TRAINING COMPLETE!")
    print("=" * 60)
    print("\nGenerated files:")
    print("  - models/fraud_model.pkl")
    print("  - models/scaler.pkl")
    print("  - models/feature_schema.json")
    print("\nNext steps:")
    print("  1. Start the ML service: python -m uvicorn app.main:app --port 8001")
    print("  2. Test prediction: curl http://localhost:8001/health")
    print("=" * 60)

if __name__ == "__main__":
    train_model()
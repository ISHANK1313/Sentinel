import json
import logging
import threading
from typing import Dict
from fastapi import FastAPI, HTTPException
from kafka import KafkaConsumer, KafkaProducer
from kafka.errors import KafkaError

from app.config import settings
from app.ml_model import FraudDetectionModel
from app.feature_engineering import FeatureEngineer

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize FastAPI app
app = FastAPI(
    title=settings.service_name,
    description="Behavioral ML fraud detection microservice",
    version="2.0.0"
)

# Initialize ML model and feature engineer
ml_model = FraudDetectionModel(model_version=settings.model_version)
feature_engineer = FeatureEngineer(redis_host="localhost", redis_port=6379)

# Kafka Producer
producer = None

def get_kafka_producer():
    """Initialize Kafka producer"""
    try:
        return KafkaProducer(
            bootstrap_servers=[settings.kafka_bootstrap_servers],
            value_serializer=lambda v: json.dumps(v).encode('utf-8'),
            acks='all',
            retries=3
        )
    except KafkaError as e:
        logger.error(f"Failed to create Kafka producer: {e}")
        return None

def process_transaction(transaction_data: Dict):
    """Process transaction with behavioral feature extraction"""
    try:
        request_id = transaction_data.get('requestId', 'unknown')
        transaction_id = transaction_data.get('transactionId')

        logger.info(f"Processing transaction: {request_id}")

        # Extract behavioral features
        features = feature_engineer.extract_features(transaction_data)

        logger.debug(f"Extracted features: {features}")

        # Generate fraud prediction
        fraud_score, confidence, prediction_class = ml_model.predict(features)

        # Create ML score message
        ml_score_message = {
            'requestId': request_id,
            'transactionId': transaction_id,
            'mlScore': fraud_score,
            'modelVersion': settings.model_version,
            'confidence': confidence,
            'predictionClass': prediction_class
        }

        # Publish to ml-scores topic
        if producer:
            future = producer.send(settings.kafka_output_topic, ml_score_message)
            producer.flush()

            try:
                record_metadata = future.get(timeout=10)
                logger.info(
                    f"Published ML score for {request_id}: "
                    f"{fraud_score}% ({prediction_class}, conf: {confidence})"
                )
            except KafkaError as e:
                logger.error(f"Failed to publish ML score: {e}")
        else:
            logger.error("Kafka producer not initialized")

    except Exception as e:
        logger.error(f"Error processing transaction: {str(e)}", exc_info=True)

def consume_transactions():
    """Kafka consumer for transactions"""
    logger.info(f"Starting Kafka consumer for topic: {settings.kafka_input_topic}")

    try:
        consumer = KafkaConsumer(
            settings.kafka_input_topic,
            bootstrap_servers=[settings.kafka_bootstrap_servers],
            group_id=settings.kafka_consumer_group,
            value_deserializer=lambda m: json.loads(m.decode('utf-8')),
            auto_offset_reset='earliest',
            enable_auto_commit=True
        )

        logger.info(f"Kafka consumer started. Listening to {settings.kafka_input_topic}...")

        for message in consumer:
            try:
                transaction_data = message.value
                process_transaction(transaction_data)
            except Exception as e:
                logger.error(f"Error processing message: {str(e)}", exc_info=True)

    except KafkaError as e:
        logger.error(f"Kafka consumer error: {e}", exc_info=True)

@app.on_event("startup")
async def startup_event():
    """Initialize on startup"""
    global producer
    producer = get_kafka_producer()

    if producer:
        logger.info("Kafka producer initialized")
    else:
        logger.warning("Kafka producer initialization failed")

    # Start consumer
    consumer_thread = threading.Thread(target=consume_transactions, daemon=True)
    consumer_thread.start()
    logger.info("Kafka consumer thread started")

@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup"""
    if producer:
        producer.close()

@app.get("/health")
def health_check():
    """Health check"""
    return {
        "status": "healthy",
        "service": settings.service_name,
        "model_version": settings.model_version,
        "model_type": "Behavioral Anomaly Detection"
    }

@app.get("/")
def root():
    """Service info"""
    return {
        "service": settings.service_name,
        "version": "2.0.0",
        "model": ml_model.get_model_info(),
        "features": feature_engineer.get_feature_names()
    }

@app.get("/model/info")
def model_info():
    """Model information"""
    return ml_model.get_model_info()

@app.get("/features")
def feature_info():
    """Feature list"""
    return {
        "features": feature_engineer.get_feature_names(),
        "description": "Behavioral features for anomaly detection"
    }

@app.post("/predict")
def predict(transaction_data: Dict):
    """Manual prediction endpoint"""
    try:
        features = feature_engineer.extract_features(transaction_data)
        fraud_score, confidence, prediction_class = ml_model.predict(features)

        return {
            "fraudScore": fraud_score,
            "confidence": confidence,
            "predictionClass": prediction_class,
            "modelVersion": settings.model_version,
            "extractedFeatures": features
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=settings.service_port)
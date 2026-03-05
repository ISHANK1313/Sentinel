from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    """Application settings"""

    # Kafka Configuration
    kafka_bootstrap_servers: str = "localhost:9092"
    kafka_consumer_group: str = "ml-engine-group"
    kafka_input_topic: str = "engine-input"
    kafka_output_topic: str = "ml-scores"

    # ML Model Configuration
    model_version: str = "fraud-detector-v1.0"
    model_path: str = "models/fraud_model.pkl"

    # Service Configuration
    service_name: str = "ML Fraud Detection Service"
    service_port: int = 8001

    class Config:
        env_file = ".env"
        case_sensitive = False

settings = Settings()
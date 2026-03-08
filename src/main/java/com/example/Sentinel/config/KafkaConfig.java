package com.example.Sentinel.config;

import com.example.Sentinel.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    // ======================== PRODUCER ========================

    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // ======================== HELPER METHOD ========================

    private <T> ConsumerFactory<String, T> buildConsumerFactory(
            Class<T> targetType, String groupId, ObjectMapper objectMapper) {

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType, objectMapper);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);  // Ignore __TypeId__ header, use target type

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> buildFactory(
            ConsumerFactory<String, T> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // Add error handler so deserialization errors don't kill the consumer
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 2)));

        return factory;
    }

    // ======================== CONSUMERS ========================

    // 1. Factory for "transactions-incoming" topic → MoneyTransferDto
    @Bean
    public ConsumerFactory<String, MoneyTransferDto> moneyTransferConsumerFactory(ObjectMapper objectMapper) {
        return buildConsumerFactory(MoneyTransferDto.class, "risk-analyzer-group", objectMapper);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MoneyTransferDto>
    kafkaListenerContainerFactory(ConsumerFactory<String, MoneyTransferDto> moneyTransferConsumerFactory) {
        return buildFactory(moneyTransferConsumerFactory);
    }

    // 2. Factory for "engine-input" topic → TransactionEnrichedDto
    @Bean
    public ConsumerFactory<String, TransactionEnrichedDto> enrichedConsumerFactory(ObjectMapper objectMapper) {
        return buildConsumerFactory(TransactionEnrichedDto.class, "rules-engine-group", objectMapper);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionEnrichedDto>
    enrichedKafkaListenerContainerFactory(ConsumerFactory<String, TransactionEnrichedDto> enrichedConsumerFactory) {
        return buildFactory(enrichedConsumerFactory);
    }

    // 3. Factory for "rule-scores" topic → RuleScoreDto
    @Bean
    public ConsumerFactory<String, RuleScoreDto> ruleScoreConsumerFactory(ObjectMapper objectMapper) {
        return buildConsumerFactory(RuleScoreDto.class, "aggregator-group", objectMapper);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RuleScoreDto>
    ruleScoreKafkaListenerContainerFactory(ConsumerFactory<String, RuleScoreDto> ruleScoreConsumerFactory) {
        return buildFactory(ruleScoreConsumerFactory);
    }

    // 4. Factory for "ml-scores" topic → MlScoreDto
    @Bean
    public ConsumerFactory<String, MlScoreDto> mlScoreConsumerFactory(ObjectMapper objectMapper) {
        return buildConsumerFactory(MlScoreDto.class, "aggregator-group", objectMapper);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MlScoreDto>
    mlScoreKafkaListenerContainerFactory(ConsumerFactory<String, MlScoreDto> mlScoreConsumerFactory) {
        return buildFactory(mlScoreConsumerFactory);
    }

    // 5. Factory for "risk-results" topic → RiskAssessmentDto
    @Bean
    public ConsumerFactory<String, RiskAssessmentDto> riskResultConsumerFactory(ObjectMapper objectMapper) {
        return buildConsumerFactory(RiskAssessmentDto.class, "websocket-broadcaster-group", objectMapper);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RiskAssessmentDto>
    riskResultKafkaListenerContainerFactory(ConsumerFactory<String, RiskAssessmentDto> riskResultConsumerFactory) {
        return buildFactory(riskResultConsumerFactory);
    }

    // ======================== TOPICS ========================

    @Bean
    public NewTopic transactionsIncomingTopic() {
        return TopicBuilder.name("transactions-incoming").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic transactionEnrichedTopic() {
        return TopicBuilder.name("engine-input").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic ruleScoresTopic() {
        return TopicBuilder.name("rule-scores").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic mlScoresTopic() {
        return TopicBuilder.name("ml-scores").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic riskResultsTopic() {
        return TopicBuilder.name("risk-results").partitions(3).replicas(1).build();
    }
}

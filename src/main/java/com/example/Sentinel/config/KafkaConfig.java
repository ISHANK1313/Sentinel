package com.example.Sentinel.config;

import com.example.Sentinel.dto.MoneyTransferDto;
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

    @Bean
    public ConsumerFactory<String, MoneyTransferDto> consumerFactory(ObjectMapper objectMapper) {

        JsonDeserializer<MoneyTransferDto> deserializer =
                new JsonDeserializer<>(MoneyTransferDto.class, objectMapper);

        deserializer.addTrustedPackages("*");

        Map<String,Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG,"risk-analyzer-group");

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MoneyTransferDto>
    kafkaListenerContainerFactory(ConsumerFactory<String, MoneyTransferDto> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, MoneyTransferDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

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

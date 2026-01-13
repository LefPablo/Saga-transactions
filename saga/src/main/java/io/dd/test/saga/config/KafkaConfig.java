package io.dd.test.saga.config;

import io.dd.test.core.kafka.event.VacationEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

import java.util.Map;

import static org.springframework.kafka.support.serializer.JacksonJsonDeserializer.TRUSTED_PACKAGES;
import static org.springframework.kafka.support.serializer.JacksonJsonDeserializer.VALUE_DEFAULT_TYPE;

@EnableKafka
@Configuration
public class KafkaConfig {

    //TODO add dlt topic plus error handler

    @Bean
    public NewTopic eventTopic(TopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName())
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, topicProperties.getMinInSyncReplicas())
                .build();
    }

    @Bean
    public Serde<Long> keySerde() {
        return new Serdes.LongSerde();
    }

    @Bean
    public Serde<SagaState> sagaStateSerde() {
        JacksonJsonSerde<SagaState> serde = new JacksonJsonSerde<>();
        serde.configure(
                Map.of(
                        TRUSTED_PACKAGES, "*",
                        VALUE_DEFAULT_TYPE, SagaState.class
                )
                , false
        );
        return serde;
    }

    @Bean
    public Serde<VacationEvent> vacationEventSerde() {
        JacksonJsonSerde<VacationEvent> serde = new JacksonJsonSerde<>();
        serde.configure(
                Map.of(
                        TRUSTED_PACKAGES, "*",
                        VALUE_DEFAULT_TYPE, VacationEvent.class
                )
                , false
        );
        return serde;
    }

    @Bean
    public Serde<Object> sagaEventSerde() {
        JacksonJsonSerde<Object> serde = new JacksonJsonSerde<>();
        serde.configure(
                Map.of(
                        TRUSTED_PACKAGES, "*",
                        VALUE_DEFAULT_TYPE, Object.class
                )
                , false
        );
        return serde;
    }

    @Bean
    public Serde<Object> sagaCommandSerde() {
        JacksonJsonSerde<Object> serde = new JacksonJsonSerde<>();
        serde.configure(
                Map.of(
                        TRUSTED_PACKAGES, "*",
                        VALUE_DEFAULT_TYPE, Object.class
                )
                , false
        );
        return serde;
    }

}

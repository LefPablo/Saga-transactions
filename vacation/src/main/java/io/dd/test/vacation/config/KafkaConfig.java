package io.dd.test.vacation.config;

import io.dd.test.vacation.api.exception.ResourceNotFoundException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import static org.springframework.kafka.retrytopic.RetryTopicConstants.DEFAULT_DLT_SUFFIX;

@EnableKafka
@Configuration
public class KafkaConfig {

    private static int BACKOFF_INTERVAL = 100;
    private static int BACKOFF_ATTEMPTS = 5;

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<Long, Object> kafkaTemplate) {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(BACKOFF_INTERVAL, BACKOFF_ATTEMPTS)
        );
        errorHandler.addNotRetryableExceptions(ResourceNotFoundException.class);
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Long, Object> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<Long, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public NewTopic commandTopic(TopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName())
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, topicProperties.getMinInSyncReplicas())
                .build();
    }

    @Bean
    public NewTopic commandDltTopic(TopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName() + DEFAULT_DLT_SUFFIX)
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, topicProperties.getMinInSyncReplicas())
                .build();
    }

}

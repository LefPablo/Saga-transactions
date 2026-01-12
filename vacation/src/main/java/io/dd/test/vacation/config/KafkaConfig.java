package io.dd.test.vacation.config;

import io.dd.test.core.kafka.command.VacationCommand;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import static org.springframework.kafka.retrytopic.RetryTopicConstants.DEFAULT_DLT_SUFFIX;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler defaultErrorHandler(
            @Value("${cfg.kafka.backoff.interval}") Integer backoffInterval,
            @Value("${cfg.kafka.backoff.attempts}") Integer backoffAttempts,
            KafkaTemplate<String, VacationCommand> kafkaTemplate) {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(backoffInterval, backoffAttempts)
        );

        errorHandler.addNotRetryableExceptions(ResourceNotFoundException.class); //TODO replace with custom ERROR

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VacationCommand> kafkaListenerContainerFactory(
            ConsumerFactory<String, VacationCommand> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, VacationCommand> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public NewTopic vacationCommandTopic(VacationTopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName())
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, topicProperties.getMinInSyncReplicas())
                .build();
    }

    @Bean
    public NewTopic vacationCommandDltTopic(VacationTopicProperties topicProperties) {
        return TopicBuilder
                .name(topicProperties.getName() + DEFAULT_DLT_SUFFIX)
                .partitions(topicProperties.getPartitions())
                .replicas(topicProperties.getReplicas())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, topicProperties.getMinInSyncReplicas())
                .build();
    }

}

package io.dd.test.resources.api.publisher;

import io.dd.test.core.kafka.event.ResourcesEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourcesKafkaPublisher {

    private final KafkaTemplate<Long, Object> kafkaTemplate;

    @Value("${app.kafka.topics.saga-event.name}")
    private String sagaEventTopic;

    public CompletableFuture<SendResult<Long, Object>> sendEvent(ResourcesEvent event) {
        ProducerRecord<Long, Object> record = new ProducerRecord<>(sagaEventTopic, event.requestId(), event);
        return sendRecord(record);
    }

    private CompletableFuture<SendResult<Long, Object>> sendRecord(ProducerRecord<Long, Object> record) {
        CompletableFuture<SendResult<Long, Object>> future = kafkaTemplate.send(record);
        return future.whenComplete((result, ex) -> {
            if (Objects.isNull(ex)) {
                log.info("Sent message [{}] to topic [{}] with offset [{}]", record.value(), record.topic(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message [{}] to topic [{}] due to: {}", record.value(), record.topic(), ex.getMessage());
            }
        });
    }

}

package io.dd.test.vacation.api.publisher;

import io.dd.test.core.kafka.event.VacationEvent;
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
public class VacationKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.saga-event.name}")
    private String sagaEventTopic;

    public CompletableFuture<SendResult<String, Object>> sendEvent(VacationEvent event) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(sagaEventTopic, event.requestId().toString(), event);
        return sendRecord(record);
    }

    private CompletableFuture<SendResult<String, Object>> sendRecord(ProducerRecord<String, Object> record) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);
        return future.whenComplete((result, ex) -> {
            if (Objects.isNull(ex)) {
                log.info("Sent message [{}] to topic [{}] with offset [{}]", record.value(), record.topic(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message [{}] to topic [{}] due to: {}", record.value(), record.topic(), ex.getMessage());
            }
        });
    }

}

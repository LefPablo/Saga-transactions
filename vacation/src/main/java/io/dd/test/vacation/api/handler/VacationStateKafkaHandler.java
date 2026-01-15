package io.dd.test.vacation.api.handler;

import io.dd.test.core.saga.SagaState;
import io.dd.test.vacation.service.VacationHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(topics = "${app.kafka.topics.saga-state.name}")
public class VacationStateKafkaHandler {

    private final VacationHandlerService service;

    @KafkaHandler
    public void handleCancelCommand(SagaState event) {
        log.info("Got vacation saga event: {}", event);
        service.processSagaStateEvent(event);
    }

}

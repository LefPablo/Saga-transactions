package io.dd.test.vacation.api.handler;

import io.dd.test.core.kafka.command.VacationCancelCommand;
import io.dd.test.core.kafka.command.VacationApproveCommand;
import io.dd.test.vacation.service.VacationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(topics = "${app.kafka.topics.vacation-command.name}")
public class VacationKafkaHandler {

    private final VacationService service;

    @KafkaHandler
    public void handleCommand(VacationApproveCommand command) {
        log.info("Got vacation command: {}", command);
        service.processCommand(command);
    }

    @KafkaHandler
    public void handleCancelCommand(VacationCancelCommand command) {
        log.info("Got vacation cancel command: {}", command);
        service.processCancelCommand(command);
    }

}

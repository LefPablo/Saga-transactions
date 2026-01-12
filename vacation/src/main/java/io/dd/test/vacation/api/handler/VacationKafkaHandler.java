package io.dd.test.vacation.api.handler;

import io.dd.test.core.kafka.command.VacationCommand;
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

    @KafkaHandler
    public void handleCommand(VacationCommand command) {
        log.info("Got vacation command: {}", command);
    }

}

package io.dd.test.accounting.api.handler;

import io.dd.test.accounting.service.AccountingService;
import io.dd.test.core.kafka.command.AccountingCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(topics = "${app.kafka.topics.accounting-command.name}")
public class AccountingKafkaHandler {

    private final AccountingService service;

    @KafkaHandler
    public void handleCommand(AccountingCommand command) {
        log.info("Got accounting command: {}", command);
        service.processCommand(command);
    }

}

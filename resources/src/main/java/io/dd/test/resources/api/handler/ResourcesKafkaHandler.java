package io.dd.test.resources.api.handler;

import io.dd.test.core.kafka.command.ResourcesCancelCommand;
import io.dd.test.core.kafka.command.ResourcesCommand;
import io.dd.test.resources.service.ResourcesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(topics = "${app.kafka.topics.resources-command.name}")
public class ResourcesKafkaHandler {

    private final ResourcesService service;

    @KafkaHandler
    public void handleCommand(ResourcesCommand command) {
        log.info("Got resources command: {}", command);
        service.processCommand(command);
    }

    @KafkaHandler
    public void handleCancelCommand(ResourcesCancelCommand command) {
        log.info("Got resources cancel command: {}", command);
        service.processCancelCommand(command);
    }

}

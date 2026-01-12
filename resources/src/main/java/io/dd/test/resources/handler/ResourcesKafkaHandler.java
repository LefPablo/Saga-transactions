package io.dd.test.resources.handler;

import io.dd.test.core.kafka.command.ResourcesCommand;
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

    @KafkaHandler
    public void handleCommand(ResourcesCommand command) {
        log.info("Got resources command: {}", command);
    }

}

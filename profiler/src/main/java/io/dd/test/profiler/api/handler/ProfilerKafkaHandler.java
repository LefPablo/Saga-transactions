package io.dd.test.profiler.api.handler;

import io.dd.test.core.kafka.command.ProfilerCommand;
import io.dd.test.profiler.service.ProfilerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(topics = "${app.kafka.topics.profiler-command.name}")
public class ProfilerKafkaHandler {

    private final ProfilerService service;

    @KafkaHandler
    public void handleCommand(ProfilerCommand command) {
        log.info("Got profiler command: {}", command);
        service.processCommand(command);
    }

}

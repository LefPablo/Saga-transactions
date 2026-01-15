package io.dd.test.profiler.service;

import io.dd.test.core.kafka.command.ProfilerCommand;
import io.dd.test.core.kafka.event.ProfilerEvent;
import io.dd.test.profiler.api.publisher.ProfilerKafkaPublisher;
import io.dd.test.profiler.persistence.model.ProfilerRequest;
import io.dd.test.profiler.persistence.model.ProfilerStatus;
import io.dd.test.profiler.persistence.repository.ProfilerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ProfilerService {

    private final ProfilerRequestRepository repository;
    private final ProfilerKafkaPublisher publisher;
    private static final UUID NIL_UUID = new UUID(0L, 0L);
    private static final AtomicInteger counter = new AtomicInteger();

    @Transactional
    public void processCommand(ProfilerCommand command) {
        ProfilerRequest request = new ProfilerRequest();
        request.setRequestId(command.requestId());
        request.setCvUuid(command.cvUuid());

        boolean updated = ! command.cvUuid().equals(NIL_UUID);
        boolean failOnCounter = counter.incrementAndGet() % 5 == 0;
        ProfilerEvent event = new ProfilerEvent(command.requestId(), updated);
        if (updated && ! failOnCounter) {
            request.setStatus(ProfilerStatus.UPDATED);
        } else {
            request.setStatus(ProfilerStatus.FAILED);
        }

        repository.save(request);
        publisher.sendEvent(event);
    }

}

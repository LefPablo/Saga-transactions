package io.dd.test.profiler.service;

import io.dd.test.core.kafka.command.ProfilerCommand;
import io.dd.test.core.kafka.event.ProfilerEvent;
import io.dd.test.profiler.api.publisher.ProfilerKafkaPublisher;
import io.dd.test.profiler.persistence.model.ProfilerRequest;
import io.dd.test.profiler.persistence.model.ProfilerStatus;
import io.dd.test.profiler.persistence.repository.ProfilerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfilerService {

    private final ProfilerRequestRepository repository;
    private final ProfilerKafkaPublisher publisher;
    private static final UUID NIL_UUID = new UUID(0L, 0L);

    //TODO joint transaction or outbox pattern
    public void processCommand(ProfilerCommand command) {
        ProfilerRequest request = new ProfilerRequest();
        request.setRequestId(command.requestId());
        request.setCvUuid(command.cvUuid());

        boolean updated = ! command.cvUuid().equals(NIL_UUID);
        ProfilerEvent event = new ProfilerEvent(command.requestId(), updated);
        if (updated) {
            request.setStatus(ProfilerStatus.UPDATED);
        } else {
            request.setStatus(ProfilerStatus.FAILED);
        }

        repository.save(request);
        publisher.sendEvent(event);
    }

}

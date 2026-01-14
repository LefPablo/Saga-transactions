package io.dd.test.resources.service;

import io.dd.test.core.kafka.command.ResourcesCancelCommand;
import io.dd.test.core.kafka.command.ResourcesCommand;
import io.dd.test.core.kafka.event.ResourcesCancelEvent;
import io.dd.test.core.kafka.event.ResourcesEvent;
import io.dd.test.resources.api.publisher.ResourcesKafkaPublisher;
import io.dd.test.resources.persistence.model.ResourcesRequest;
import io.dd.test.resources.persistence.model.ResourcesStatus;
import io.dd.test.resources.persistence.repository.ResourcesRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ResourcesService {

    private final ResourcesRequestRepository repository;
    private final ResourcesKafkaPublisher publisher;

    @Transactional
    public void processCommand(ResourcesCommand command) {
        ResourcesRequest request = new ResourcesRequest();
        request.setRequestId(command.requestId());
        request.setPeriodFrom(command.periodFrom());
        request.setPeriodTo(command.periodTo());

        boolean passed = ChronoUnit.DAYS.between(command.periodFrom(), command.periodTo()) <= 25;
        ResourcesEvent event = new ResourcesEvent(command.requestId(), passed);
        if (passed) {
            request.setStatus(ResourcesStatus.PASSED);
        } else {
            request.setStatus(ResourcesStatus.FAILED);
        }

        repository.save(request);
        publisher.sendEvent(event);
    }

    @Transactional
    public void processCancelCommand(ResourcesCancelCommand command) {
        ResourcesRequest request = repository.findByRequestId(command.requestId()).orElseThrow();
        request.setStatus(ResourcesStatus.CANCELED);

        ResourcesCancelEvent event = new ResourcesCancelEvent(command.requestId());
        publisher.sendEvent(event);
    }

}

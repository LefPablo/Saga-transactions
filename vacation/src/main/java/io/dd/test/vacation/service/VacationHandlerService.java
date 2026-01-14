package io.dd.test.vacation.service;

import io.dd.test.core.ProcessStatus;
import io.dd.test.core.kafka.command.VacationApproveCommand;
import io.dd.test.core.kafka.command.VacationCancelCommand;
import io.dd.test.core.kafka.event.VacationApprovedEvent;
import io.dd.test.core.kafka.event.VacationCancelEvent;
import io.dd.test.vacation.api.exception.ResourceNotFoundException;
import io.dd.test.vacation.api.publisher.VacationKafkaPublisher;
import io.dd.test.vacation.persistence.model.VacationRequest;
import io.dd.test.vacation.persistence.repository.VacationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VacationHandlerService {

    private final VacationRequestRepository repository;
    private final VacationKafkaPublisher publisher;

    @Transactional
    public void processCommand(VacationApproveCommand command) {
        VacationRequest request = repository.findById(command.requestId())
                .orElseThrow(() -> new ResourceNotFoundException("Can't find vacation request by id:" + command.requestId()));
        request.setStatus(ProcessStatus.APPROVED);

        VacationApprovedEvent event = new VacationApprovedEvent(request.getId());

        repository.save(request);
        publisher.sendEvent(event);
    }

    @Transactional
    public void processCancelCommand(VacationCancelCommand command) {
        VacationRequest request = repository.findById(command.requestId())
                .orElseThrow(() -> new ResourceNotFoundException("Can't find vacation request by id:" + command.requestId()));
        request.setStatus(ProcessStatus.REJECTED);

        VacationCancelEvent event = new VacationCancelEvent(command.requestId());
        publisher.sendEvent(event);
    }

}

package io.dd.test.vacation.service;

import io.dd.test.core.ProcessStatus;
import io.dd.test.core.kafka.command.VacationApproveCommand;
import io.dd.test.core.kafka.command.VacationCancelCommand;
import io.dd.test.core.kafka.event.VacationApprovedEvent;
import io.dd.test.core.kafka.event.VacationCancelEvent;
import io.dd.test.core.saga.SagaState;
import io.dd.test.vacation.api.exception.ResourceNotFoundException;
import io.dd.test.vacation.api.publisher.VacationKafkaPublisher;
import io.dd.test.vacation.mapper.VacationRequestStateMapper;
import io.dd.test.vacation.persistence.model.VacationRequest;
import io.dd.test.vacation.persistence.repository.VacationRequestHistoryRepository;
import io.dd.test.vacation.persistence.repository.VacationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VacationHandlerService {

    private final VacationRequestRepository requestRepository;
    private final VacationRequestHistoryRepository requestHistoryRepository;
    private final VacationKafkaPublisher publisher;
    private final VacationRequestStateMapper stateMapper;

    @Transactional
    public void processCommand(VacationApproveCommand command) {
        VacationRequest request = requestRepository.findById(command.requestId())
                .orElseThrow(() -> new ResourceNotFoundException("Can't find vacation request by id:" + command.requestId()));
        request.setStatus(ProcessStatus.APPROVED);

        VacationApprovedEvent event = new VacationApprovedEvent(request.getId());

        requestRepository.save(request);
        publisher.sendEvent(event);
    }

    @Transactional
    public void processCancelCommand(VacationCancelCommand command) {
        VacationRequest request = requestRepository.findById(command.requestId())
                .orElseThrow(() -> new ResourceNotFoundException("Can't find vacation request by id:" + command.requestId()));
        request.setStatus(ProcessStatus.REJECTED);

        VacationCancelEvent event = new VacationCancelEvent(command.requestId());
        publisher.sendEvent(event);
    }

    public void processSagaStateEvent(SagaState event) {
        requestHistoryRepository.save(stateMapper.toEntity(event));
    }

}

package io.dd.test.vacation.service;

import io.dd.test.core.ProcessStatus;
import io.dd.test.core.kafka.command.VacationCancelCommand;
import io.dd.test.core.kafka.command.VacationCommand;
import io.dd.test.core.kafka.event.VacationApprovedEvent;
import io.dd.test.core.kafka.event.VacationCancelEvent;
import io.dd.test.core.kafka.event.VacationEvent;
import io.dd.test.vacation.api.dto.CreateVacationRequestDto;
import io.dd.test.vacation.api.dto.VacationRequestDto;
import io.dd.test.vacation.api.publisher.VacationKafkaPublisher;
import io.dd.test.vacation.mapper.VacationRequestMapper;
import io.dd.test.vacation.persistence.model.VacationRequest;
import io.dd.test.vacation.persistence.repository.VacationRequestRepository;
import io.dd.test.vacation.persistence.specification.SearchCriteria;
import io.dd.test.vacation.persistence.specification.VacationRequestSpecification;
import io.dd.test.vacation.util.CriteriaParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VacationService {

    private final VacationRequestRepository repository;
    private final VacationKafkaPublisher publisher;
    private final VacationRequestMapper mapper;

    public VacationRequestDto createRequest(CreateVacationRequestDto createRequest) {
        VacationRequest request = mapper.toEntity(createRequest, ProcessStatus.CREATED);
        request = repository.save(request);

        VacationEvent event = mapper.toEvent(request);
        publisher.sendEvent(event);

        return mapper.toDto(request);
    }

    public VacationRequestDto getRequest(Long requestId) {
        VacationRequest request = repository.findById(requestId).orElseThrow();
        return mapper.toDto(request);
    }

    public Page<VacationRequestDto> getRequestsByQuery(List<String> queryParams, Pageable pageable) {
        Specification<VacationRequest> spec = Specification.where((root, query, cb) -> null);

        if (Objects.nonNull(queryParams)) {
            for (String criteriaString : queryParams) {
                SearchCriteria criteria = CriteriaParser.parseCriteriaString(criteriaString);
                spec = spec.and(new VacationRequestSpecification(criteria));
            }
        }

        Page<VacationRequest> results = repository.findAll(spec, pageable);
        return results.map(mapper::toDto);
    }

    //TODO joint transaction or outbox pattern
    @Transactional
    public void processCommand(VacationCommand command) {
        VacationRequest request = repository.findById(command.requestId()).orElseThrow();
        request.setStatus(ProcessStatus.APPROVED);

        VacationApprovedEvent event = new VacationApprovedEvent(request.getId());

        repository.save(request);
        publisher.sendEvent(event);
    }

    @Transactional
    public void processCancelCommand(VacationCancelCommand command) {
        VacationRequest request = repository.findById(command.requestId()).orElseThrow();
        request.setStatus(ProcessStatus.REJECTED);

        VacationCancelEvent event = new VacationCancelEvent(command.requestId());
        publisher.sendEvent(event);
    }

}

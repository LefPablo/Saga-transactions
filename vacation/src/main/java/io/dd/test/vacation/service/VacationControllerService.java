package io.dd.test.vacation.service;

import io.dd.test.core.ProcessStatus;
import io.dd.test.core.kafka.event.VacationEvent;
import io.dd.test.core.saga.SagaState;
import io.dd.test.vacation.api.dto.CreateVacationRequestDto;
import io.dd.test.vacation.api.dto.VacationRequestDto;
import io.dd.test.vacation.api.exception.ResourceNotFoundException;
import io.dd.test.vacation.api.publisher.VacationKafkaPublisher;
import io.dd.test.vacation.mapper.VacationRequestMapper;
import io.dd.test.vacation.mapper.VacationRequestStateMapper;
import io.dd.test.vacation.persistence.model.VacationRequest;
import io.dd.test.vacation.persistence.model.VacationRequestState;
import io.dd.test.vacation.persistence.repository.VacationRequestHistoryRepository;
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
public class VacationControllerService {

    private final VacationRequestRepository requestRepository;
    private final VacationRequestHistoryRepository requestHistoryRepository;
    private final VacationKafkaPublisher publisher;
    private final VacationRequestMapper requestMapper;
    private final VacationRequestStateMapper requestStateMapper;

    @Transactional
    public VacationRequestDto createRequest(CreateVacationRequestDto createRequest) {
        VacationRequest request = requestMapper.toEntity(createRequest, ProcessStatus.CREATED);
        request = requestRepository.save(request);

        VacationEvent event = requestMapper.toEvent(request);
        publisher.sendEvent(event);

        return requestMapper.toDto(request);
    }

    public VacationRequestDto getRequest(Long requestId) {
        VacationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Can't find vacation request by id:" + requestId));
        return requestMapper.toDto(request);
    }

    public Page<VacationRequestDto> getRequestsByQuery(List<String> queryParams, Pageable pageable) {
        Specification<VacationRequest> spec = Specification.where((root, query, cb) -> null);

        if (Objects.nonNull(queryParams)) {
            for (String criteriaString : queryParams) {
                SearchCriteria criteria = CriteriaParser.parseCriteriaString(criteriaString);
                spec = spec.and(new VacationRequestSpecification(criteria));
            }
        }

        Page<VacationRequest> results = requestRepository.findAll(spec, pageable);
        return results.map(requestMapper::toDto);
    }

    public List<SagaState> getRequestHistory(Long requestId) {
        List<VacationRequestState> requestStates = requestHistoryRepository.findAllById_RequestIdOrderById_CreatedAtDesc(requestId);
        return requestStates.stream().map(requestStateMapper::toSagaState).toList();
    }
}

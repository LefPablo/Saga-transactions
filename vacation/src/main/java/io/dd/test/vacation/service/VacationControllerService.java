package io.dd.test.vacation.service;

import io.dd.test.core.ProcessStatus;
import io.dd.test.core.kafka.event.VacationEvent;
import io.dd.test.vacation.api.dto.CreateVacationRequestDto;
import io.dd.test.vacation.api.dto.VacationRequestDto;
import io.dd.test.vacation.api.exception.ResourceNotFoundException;
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
public class VacationControllerService {

    private final VacationRequestRepository repository;
    private final VacationKafkaPublisher publisher;
    private final VacationRequestMapper mapper;

    @Transactional
    public VacationRequestDto createRequest(CreateVacationRequestDto createRequest) {
        VacationRequest request = mapper.toEntity(createRequest, ProcessStatus.CREATED);
        request = repository.save(request);

        VacationEvent event = mapper.toEvent(request);
        publisher.sendEvent(event);

        return mapper.toDto(request);
    }

    public VacationRequestDto getRequest(Long requestId) {
        VacationRequest request = repository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Can't find vacation request by id:" + requestId));
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

}

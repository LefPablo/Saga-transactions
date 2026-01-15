package io.dd.test.vacation.api.controller;

import io.dd.test.vacation.api.dto.CreateVacationRequestDto;
import io.dd.test.vacation.api.dto.VacationRequestDto;
import io.dd.test.vacation.api.dto.VacationStateDto;
import io.dd.test.vacation.service.VacationControllerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/vacation")
public class VacationController {

    private final VacationControllerService service;

    @PostMapping("/request")
    @Operation(
            summary = "Create a new vacation request",
            description = "Creates a new vacation request for an employee<br>" +
                    "<strong>By default each 5th request will fail on profiler service</strong><br>" +
                    "<strong>To simulate fail on specific service set following values:</strong><br>" +
                    "for accounting set budget > 100<br>" +
                    "for resources set time that periodTo - periodFrom > 25 days<br>" +
                    "for profiler set cvUuid to 00000000-0000-0000-0000-000000000000"
    )
    public VacationRequestDto createVacationRequest(@Valid @RequestBody CreateVacationRequestDto createRequest) {
        log.info("Create vacation request: {}", createRequest);
        return service.createRequest(createRequest);
    }

    @GetMapping("/request/{requestId}")
    @Operation(
            summary = "Get vacation request by ID",
            description = "Retrieves detailed information about a specific vacation request"
    )
    public VacationRequestDto getVacationRequest(@PathVariable Long requestId) {
        log.info("Get vacation request by id: {}", requestId);
        return service.getRequest(requestId);
    }

    @GetMapping("/request/{requestId}/history")
    @Operation(
            summary = "Get vacation request history by ID",
            description = "Retrieves detailed information about a specific vacation request history"
    )
    public List<VacationStateDto> getVacationRequestHistory(@PathVariable Long requestId) {
        log.info("Get vacation request history by id: {}", requestId);
        return service.getRequestHistory(requestId);
    }

    @GetMapping("/request")
    @Operation(
            summary = "Get vacation requests page by query params",
            description = "Retrieves page of detailed information about vacation requests by query params"
    )
    public Page<VacationRequestDto> getVacationRequests(
            @Parameter(example = "[\"status=APPROVED\", \"budget>50\"]")
            @RequestParam(name = "queryParams", required = false) List<String> queryParams,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = {"status"}) Pageable pageable) {
        log.info("Get vacation requests by query: {}", queryParams);
        return service.getRequestsByQuery(queryParams, pageable);
    }

}

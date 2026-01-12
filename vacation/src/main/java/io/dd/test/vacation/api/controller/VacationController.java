package io.dd.test.vacation.api.controller;

import io.dd.test.vacation.api.dto.CreateVacationRequestDto;
import io.dd.test.vacation.api.dto.VacationRequestDto;
import io.dd.test.vacation.service.VacationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/vacation")
public class VacationController {

    private final VacationService service;

    @PostMapping("/request")
    public VacationRequestDto createVacationRequest(@Valid @RequestBody CreateVacationRequestDto createRequest) {
        log.info("Create vacation request: {}", createRequest);
        return service.createRequest(createRequest);
    }

    @GetMapping("/request/{requestId}")
    public VacationRequestDto getVacationRequest(@PathVariable Long requestId) {
        log.info("Get vacation request by id: {}", requestId);
        return service.getRequest(requestId);
    }
}

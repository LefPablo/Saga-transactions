package io.dd.test.vacation.api.controller;

import io.dd.test.core.ProcessStatus;
import io.dd.test.vacation.api.dto.CreateVacationRequestDto;
import io.dd.test.vacation.api.dto.VacationRequestDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("api/v1/vacation")
public class VacationController {

    @PostMapping("/request")
    public VacationRequestDto createVacationRequest(@Valid @RequestBody CreateVacationRequestDto createRequest) {
        log.info("Create vacation request: {}", createRequest);
        return new VacationRequestDto(1L, UUID.randomUUID(), LocalDate.now(), LocalDate.now(), BigDecimal.ONE, ProcessStatus.CREATED);
    }

    @GetMapping("/request/{requestId}")
    public VacationRequestDto getVacationRequest(@PathVariable Long requestId) {
        log.info("Get vacation request by id: {}", requestId);
        return new VacationRequestDto(1L, UUID.randomUUID(), LocalDate.now(), LocalDate.now(), BigDecimal.ONE, ProcessStatus.CREATED);
    }
}

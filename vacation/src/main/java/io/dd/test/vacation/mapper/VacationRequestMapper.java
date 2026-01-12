package io.dd.test.vacation.mapper;

import io.dd.test.core.ProcessStatus;
import io.dd.test.core.kafka.event.VacationEvent;
import io.dd.test.vacation.api.dto.CreateVacationRequestDto;
import io.dd.test.vacation.api.dto.VacationRequestDto;
import io.dd.test.vacation.persistence.model.VacationRequest;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true))
public interface VacationRequestMapper {

    VacationRequest toEntity(VacationRequestDto requestDto);
    VacationRequestDto toDto(VacationRequest request);

    @Mapping(target = "status", expression = "java(status)")
    VacationRequest toEntity(CreateVacationRequestDto createRequest, ProcessStatus status);

    @Mapping(source = "request.id", target = "requestId")
    VacationEvent toEvent(VacationRequest request);

}

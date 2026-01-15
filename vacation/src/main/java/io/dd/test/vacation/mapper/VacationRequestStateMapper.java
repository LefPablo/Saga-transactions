package io.dd.test.vacation.mapper;

import io.dd.test.core.saga.SagaState;
import io.dd.test.vacation.persistence.model.VacationRequestState;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true))
public interface VacationRequestStateMapper {

    SagaState toSagaState(VacationRequestState requestState);
    VacationRequestState toEntity(SagaState state);

}

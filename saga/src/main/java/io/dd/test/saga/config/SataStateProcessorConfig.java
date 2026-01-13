package io.dd.test.saga.config;

import io.dd.test.core.kafka.command.*;
import io.dd.test.core.kafka.event.VacationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableKafkaStreams
public class SataStateProcessorConfig {

    private final Serde<Long> keySerde;
    private final Serde<Object> sagaCommandSerde;

    @Bean
    public KStream<Long, Object> sagaStateProcessor(KTable<Long, VacationEvent> sagaDataTable,
                                                    KTable<Long, SagaState> sagaStateTable,
                                                    @Value("${app.kafka.topics.vacation-command.name}") String vacationCommandTopic,
                                                    @Value("${app.kafka.topics.profiler-command.name}") String profilerCommandTopic,
                                                    @Value("${app.kafka.topics.accounting-command.name}") String accountingCommandTopic,
                                                    @Value("${app.kafka.topics.resources-command.name}") String resourcesCommandTopic
                                                    ) {
        KStream<Long, Object> commandStream = sagaStateTable
                .toStream()
                .join(sagaDataTable, AugmentedSagaState::new)
                .peek((key, command) -> log.info("Sending command: {} for sagaId: {}", command.getClass().getSimpleName(), key))
                .flatMapValues((sagaId, augmentedSagaState) -> switch ((augmentedSagaState.state).status()) {
                    case CREATED -> List.of(createAllocateBudgetCommand(augmentedSagaState));
                    case BUDGET_ALLOCATED -> List.of(createCheckResourcesCommand(augmentedSagaState));
                    case RESOURCES_PASSED -> List.of(createUpdateProfileCommand(augmentedSagaState));
                    case PROFILE_UPDATED -> List.of(createApproveVacationCommand(augmentedSagaState));
                    case PROFILE_UPDATE_FAILED -> List.of(createResourcesCancelCommand(augmentedSagaState));
                    case RESOURCES_FAILED, RESOURCES_CANCELED -> List.of(createAccountingCancelCommand(augmentedSagaState));
                    case BUDGET_FAILED, BUDGET_ALLOCATION_CANCELED -> List.of(createVacationCancelCommand(augmentedSagaState));
                    default -> List.of(); //handle illegal state logic
                });

        sendCommands(commandStream, vacationCommandTopic, profilerCommandTopic, accountingCommandTopic, resourcesCommandTopic);

        return commandStream;
    }

    private Object createVacationCancelCommand(AugmentedSagaState augmentedSagaState) {
        return new VacationCancelCommand(augmentedSagaState.sagaData.requestId());

    }

    private Object createAccountingCancelCommand(AugmentedSagaState augmentedSagaState) {
        return new AccountingCancelCommand(augmentedSagaState.sagaData.requestId());
    }

    private Object createResourcesCancelCommand(AugmentedSagaState augmentedSagaState) {
        return new ResourcesCancelCommand(augmentedSagaState.sagaData.requestId());
    }

    private Object createApproveVacationCommand(AugmentedSagaState augmentedSagaState) {
        return new VacationCommand(augmentedSagaState.sagaData.requestId());
    }

    private Object createUpdateProfileCommand(AugmentedSagaState augmentedSagaState) {
        return new ProfilerCommand(augmentedSagaState.sagaData.requestId(), augmentedSagaState.sagaData.cvUuid());
    }

    private Object createCheckResourcesCommand(AugmentedSagaState augmentedSagaState) {
        return new ResourcesCommand(augmentedSagaState.sagaData.requestId(), augmentedSagaState.sagaData.periodFrom(), augmentedSagaState.sagaData.periodTo());
    }

    private Object createAllocateBudgetCommand(AugmentedSagaState augmentedSagaState) {
        return new AccountingCommand(augmentedSagaState.sagaData.requestId(), augmentedSagaState.sagaData.budget());
    }

    private void sendCommands(KStream<Long, Object> commandStream, String vacationCommandTopic, String profilerCommandTopic, String accountingCommandTopic, String resourcesCommandTopic) {
        commandStream.filter((key, command) -> command instanceof VacationCommand || command instanceof VacationCancelCommand)
                .to(vacationCommandTopic, Produced.with(keySerde, sagaCommandSerde));
        commandStream.filter((key, command) -> command instanceof AccountingCommand || command instanceof AccountingCancelCommand)
                .to(accountingCommandTopic, Produced.with(keySerde, sagaCommandSerde));
        commandStream.filter((key, command) -> command instanceof ResourcesCommand || command instanceof ResourcesCancelCommand)
                .to(resourcesCommandTopic, Produced.with(keySerde, sagaCommandSerde));
        commandStream.filter((key, command) -> command instanceof ProfilerCommand)
                .to(profilerCommandTopic, Produced.with(keySerde, sagaCommandSerde));
    }

    private record AugmentedSagaState(SagaState state, VacationEvent sagaData) {}

}

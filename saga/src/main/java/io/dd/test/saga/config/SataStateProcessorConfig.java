package io.dd.test.saga.config;

import io.dd.test.core.kafka.command.AccountingCommand;
import io.dd.test.core.kafka.command.VacationCommand;
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

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
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
        KStream<Long, Object> commandStream = sagaStateTable.toStream()
                .join(sagaDataTable, AugmentedSagaState::new)
                .flatMapValues((sagaId, augmentedSagaState) -> switch (augmentedSagaState.state.status()) {
                    case CREATED -> List.of(createAllocateBudgetCommand(augmentedSagaState));
                    default -> List.of(); //handle illegal state logic
                })
                .peek((key, command) -> log.info("Sending command: {} for sagaId: {}", command.getClass().getSimpleName(), key));

        sendCommands(commandStream, vacationCommandTopic, profilerCommandTopic, accountingCommandTopic, resourcesCommandTopic);

        return commandStream;
    }

    private void sendCommands(KStream<Long, Object> commandStream, String vacationCommandTopic, String profilerCommandTopic, String accountingCommandTopic, String resourcesCommandTopic) {
        commandStream.filter((key, command) -> command instanceof VacationCommand) //TODO add failed command
                .to(vacationCommandTopic, Produced.with(keySerde, sagaCommandSerde));
    }

    private Object createAllocateBudgetCommand(AugmentedSagaState augmentedSagaState) {
        return new AccountingCommand(augmentedSagaState.sagaData.requestId(), augmentedSagaState.sagaData.budget());
    }

    private record AugmentedSagaState(SagaState state, VacationEvent sagaData) {}

}

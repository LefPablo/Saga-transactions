package io.dd.test.saga.config;

import io.dd.test.core.ProcessStatus;
import io.dd.test.core.kafka.event.AccountingEvent;
import io.dd.test.core.kafka.event.VacationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

import static io.dd.test.core.ProcessStatus.CREATED;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SagaStateReducerConfig {

    public static final String SAGA_STATE_STORE = "saga-state-store";
    public static final String SAGA_DATA_STORE = "saga-data-store";

    private final Serde<Long> keySerde;
    private final Serde<Object> sagaEventSerde; //TODO
    private final Serde<SagaState> sagaStateSerde;
    private final Serde<VacationEvent> requestCreatedEventSerdeSerde;

    @Bean
    public KStream<Long, Object> sagaEventStream(StreamsBuilder streamsBuilder,
                                          @Value("${app.kafka.topics.vacation-event.name}") String vacationEventTopic,
                                          @Value("${app.kafka.topics.profiler-event.name}") String profilerEventTopic,
                                          @Value("${app.kafka.topics.accounting-event.name}") String accountingEventTopic,
                                          @Value("${app.kafka.topics.resources-event.name}") String resourcesEventTopic
                                          ) {
        return streamsBuilder.stream(
                Set.of(vacationEventTopic, profilerEventTopic, accountingEventTopic, resourcesEventTopic),
                Consumed.with(keySerde, sagaEventSerde)
        );
    }

    @Bean
    public KTable<Long, VacationEvent> sagaDataTable(KStream<Long, Object> sagaEventStream) {
        return sagaEventStream
                .filter((key, event) -> event instanceof VacationEvent) //TODO vacationCreateRequestEvent?
                .mapValues((value) -> (VacationEvent) value)
                .toTable(Materialized.<Long, VacationEvent>as(Stores.inMemoryKeyValueStore(SAGA_DATA_STORE))
                        .withKeySerde(keySerde)
                        .withValueSerde(requestCreatedEventSerdeSerde));
    }

    @Bean
    public KTable<Long, SagaState> sagaStateTable(KStream<Long, Object> sagaEventStream) {
        return sagaEventStream
                .peek(((uuid, event) -> log.info("Received event {} for request id {}", event, uuid))) //TODO abstract SagaEvent with requestID
                .groupByKey()
                .aggregate(
                        () -> null,
                        (sagaId, event, currentState) -> {
                            if (currentState == null) {
                                return createNewSagaState((VacationEvent) event);
                            }

                            return switch (currentState.status()) {
                                case CREATED -> handleCreatedState(currentState, event);
                                //TODO add rest of statuses
                                default -> updateSagaState(currentState, ProcessStatus.ILLEGAL_STATE);
                            };
                        },
                        Materialized.<Long, SagaState>as(Stores.inMemoryKeyValueStore(SAGA_STATE_STORE))
                                .withKeySerde(keySerde)
                                .withValueSerde(sagaStateSerde)
                );
    }

    private SagaState handleCreatedState(SagaState currentState, Object event) {
        if (event instanceof AccountingEvent) {
            return updateSagaState(currentState, ProcessStatus.BUDGET_ALLOCATED);
        }
//        } else if (event instanceof AccountingFailedEvent) {
//            return updateSagaState(currentState, ProcessStatus.BUDGET_FAILED, "budget allocation failed: " + "requestId");
//          }
        return updateSagaState(currentState, ProcessStatus.ILLEGAL_STATE);
    }

    private SagaState createNewSagaState(VacationEvent event) {
        return new SagaState(event.requestId(), CREATED);
    }

    private SagaState updateSagaState(SagaState state, ProcessStatus newStatus) {
        return new SagaState(state.requestId(), newStatus);
    }

    private SagaState updateSagaState(SagaState state, ProcessStatus newStatus, String errorMessage) {
        return new SagaState(state.requestId(), newStatus, errorMessage);
    }


}

package io.dd.test.saga.config;

import io.dd.test.core.ProcessStatus;
import io.dd.test.core.kafka.event.*;
import io.dd.test.core.saga.SagaState;
import lombok.Getter;
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
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.Set;

import static io.dd.test.core.ProcessStatus.CREATED;

@Getter
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableKafkaStreams
public class SagaStateReducerConfig {

    public static final String SAGA_STATE_STORE = "saga-state-store";
    public static final String SAGA_DATA_STORE = "saga-data-store";

    private final Serde<Long> keySerde;
    private final Serde<Object> sagaEventSerde;
    private final Serde<SagaState> sagaStateSerde;
    private final Serde<VacationEvent> vacationEventSerde;

    @Bean
    public KStream<Long, Object> sagaEventStream(StreamsBuilder streamsBuilder,
                                          @Value("${app.kafka.topics.saga-event.name}") String sagaEventTopic
                                          ) {
        return streamsBuilder.stream(
                Set.of(sagaEventTopic),
                Consumed.with(keySerde, sagaEventSerde)
        );
    }

    @Bean
    public KTable<Long, VacationEvent> sagaDataTable(KStream<Long, Object> sagaEventStream) {
        return sagaEventStream
                .filter((key, event) -> event instanceof VacationEvent)
                .mapValues((value) -> (VacationEvent) value)
                .toTable(Materialized.<Long, VacationEvent>as(Stores.inMemoryKeyValueStore(SAGA_DATA_STORE))
                        .withKeySerde(keySerde)
                        .withValueSerde(vacationEventSerde));
    }

    @Bean
    public KTable<Long, SagaState> sagaStateTable(KStream<Long, Object> sagaEventStream) {
        return sagaEventStream
                .peek(((uuid, event) -> log.info("Received event {} for request id {}", event, uuid)))
                .groupByKey()
                .aggregate(
                        () -> null,
                        (sagaId, event, currentState) -> {
                            if (currentState == null) {
                                return createNewSagaState((VacationEvent) event);
                            }

                            return switch (currentState.status()) {
                                case CREATED -> handleCreatedState(currentState, event);
                                case BUDGET_ALLOCATED -> handleAllocationState(currentState, event);
                                case RESOURCES_PASSED -> handleResourcesState(currentState, event);
                                case PROFILE_UPDATED -> handleProfilerState(currentState, event);
                                case BUDGET_FAILED, BUDGET_ALLOCATION_CANCELED -> handleAllocationFailedOrCanceledState(currentState, event);
                                case RESOURCES_FAILED, RESOURCES_CANCELED -> handleResourcesFailedOrCanceledState(currentState, event);
                                case PROFILE_UPDATE_FAILED -> handleProfilerFailedState(currentState, event);
                                default -> updateSagaState(currentState, ProcessStatus.ILLEGAL_STATE);
                            };
                        },
                        Materialized.<Long, SagaState>as(Stores.inMemoryKeyValueStore(SAGA_STATE_STORE))
                                .withKeySerde(keySerde)
                                .withValueSerde(sagaStateSerde)
                );
    }

    private SagaState handleProfilerFailedState(SagaState currentState, Object event) {
        if (event instanceof ResourcesCancelEvent) {
            return updateSagaState(currentState, ProcessStatus.RESOURCES_CANCELED);
        }
        return updateSagaState(currentState, ProcessStatus.ILLEGAL_STATE);
    }

    private SagaState handleResourcesFailedOrCanceledState(SagaState currentState, Object event) {
        if (event instanceof AccountingCanceledEvent) {
            return updateSagaState(currentState, ProcessStatus.BUDGET_ALLOCATION_CANCELED);
        }
        return updateSagaState(currentState, ProcessStatus.ILLEGAL_STATE);
    }

    private SagaState handleAllocationFailedOrCanceledState(SagaState currentState, Object event) {
        if (event instanceof VacationCancelEvent) {
            return updateSagaState(currentState, ProcessStatus.REJECTED);
        }
        return updateSagaState(currentState, ProcessStatus.ILLEGAL_STATE);
    }

    private SagaState handleProfilerState(SagaState currentState, Object event) {
        if (event instanceof VacationApprovedEvent) {
            return updateSagaState(currentState, ProcessStatus.APPROVED);
        }
        return updateSagaState(currentState, ProcessStatus.ILLEGAL_STATE);
    }

    private SagaState handleResourcesState(SagaState currentState, Object event) {
        if (event instanceof ProfilerEvent(Long requestId, Boolean updated)) {
            if (updated) {
                return updateSagaState(currentState, ProcessStatus.PROFILE_UPDATED);
            } else {
                return updateSagaState(currentState, ProcessStatus.PROFILE_UPDATE_FAILED, "profile update failed: " + requestId);
            }
        }
        return updateSagaState(currentState, ProcessStatus.ILLEGAL_STATE);
    }

    private SagaState handleAllocationState(SagaState currentState, Object event) {
        if (event instanceof ResourcesEvent(Long requestId, Boolean passed)) {
            if (passed) {
                return updateSagaState(currentState, ProcessStatus.RESOURCES_PASSED);
            } else {
                return updateSagaState(currentState, ProcessStatus.RESOURCES_FAILED, "resources check failed: " + requestId);
            }
        }
        return updateSagaState(currentState, ProcessStatus.ILLEGAL_STATE);
    }

    private SagaState handleCreatedState(SagaState currentState, Object event) {
        if (event instanceof AccountingEvent(Long requestId, Boolean allocated)) {
            if (allocated) {
                return updateSagaState(currentState, ProcessStatus.BUDGET_ALLOCATED);
            } else {
                return updateSagaState(currentState, ProcessStatus.BUDGET_FAILED, "budget allocation failed: " + requestId);
            }
        }
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

package io.dd.test.accounting.service;

import io.dd.test.accounting.api.publisher.AccountingKafkaPublisher;
import io.dd.test.accounting.persistence.model.AccountingRequest;
import io.dd.test.accounting.persistence.model.AccountingStatus;
import io.dd.test.accounting.persistence.repository.AccountingRequestRepository;
import io.dd.test.core.kafka.command.AccountingCancelCommand;
import io.dd.test.core.kafka.command.AccountingCommand;
import io.dd.test.core.kafka.event.AccountingCanceledEvent;
import io.dd.test.core.kafka.event.AccountingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountingService {

    private final AccountingRequestRepository repository;
    private final AccountingKafkaPublisher publisher;

    //TODO joint transaction or outbox pattern
    public void processCommand(AccountingCommand command) {
        AccountingRequest request = new AccountingRequest();
        request.setRequestId(command.requestId());
        request.setBudget(command.budget());

        boolean allocated = command.budget().compareTo(BigDecimal.valueOf(100)) < 1;
        AccountingEvent event = new AccountingEvent(command.requestId(), allocated);
        if (allocated) {
            request.setStatus(AccountingStatus.ALLOCATED);
        } else {
            request.setStatus(AccountingStatus.FAILED);
        }

        repository.save(request);
        publisher.sendEvent(event);
    }

    @Transactional
    public void processCancelCommand(AccountingCancelCommand command) {
        AccountingRequest request = repository.findByRequestId(command.requestId()).orElseThrow();
        request.setStatus(AccountingStatus.CANCELED);

        AccountingCanceledEvent event = new AccountingCanceledEvent(command.requestId());
        publisher.sendEvent(event);
    }

}

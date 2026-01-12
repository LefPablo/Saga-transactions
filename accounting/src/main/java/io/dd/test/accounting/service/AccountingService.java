package io.dd.test.accounting.service;

import io.dd.test.accounting.api.publisher.AccountingKafkaPublisher;
import io.dd.test.accounting.persistence.model.AccountingRequest;
import io.dd.test.accounting.persistence.repository.AccountingRequestRepository;
import io.dd.test.core.kafka.command.AccountingCommand;
import io.dd.test.core.kafka.event.AccountingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        repository.save(request);
        publisher.sendEvent(event);
    }

}

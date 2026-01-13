package io.dd.test.accounting.persistence.repository;

import io.dd.test.accounting.persistence.model.AccountingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountingRequestRepository extends JpaRepository<AccountingRequest, Long> {

    Optional<AccountingRequest> findByRequestId(Long requestId);

}

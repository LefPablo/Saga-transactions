package io.dd.test.vacation.persistence.repository;

import io.dd.test.vacation.persistence.model.VacationRequestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacationRequestHistoryRepository extends JpaRepository<VacationRequestState, Long> {

    List<VacationRequestState> findAllByRequestIdOrderByCreatedAtAsc(Long requestId);

}

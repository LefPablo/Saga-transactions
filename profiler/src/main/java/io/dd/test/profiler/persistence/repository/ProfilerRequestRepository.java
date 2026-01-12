package io.dd.test.profiler.persistence.repository;

import io.dd.test.profiler.persistence.model.ProfilerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfilerRequestRepository extends JpaRepository<ProfilerRequest, Long> {
}

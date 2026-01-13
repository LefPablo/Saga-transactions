package io.dd.test.resources.persistence.repository;

import io.dd.test.resources.persistence.model.ResourcesRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourcesRequestRepository extends JpaRepository<ResourcesRequest, Long> {

    Optional<ResourcesRequest> findByRequestId(Long requestId);

}

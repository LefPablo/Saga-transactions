package io.dd.test.resources.persistence.repository;

import io.dd.test.resources.persistence.model.ResourcesRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourcesRequestRepository extends JpaRepository<ResourcesRequest, Long> {
}

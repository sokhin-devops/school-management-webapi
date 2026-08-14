package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.School;

public interface SchoolRepository extends JpaRepository<School, UUID> {

	List<School> findByTenantIdOrderByCreatedAtAsc(UUID tenantId);

	Optional<School> findFirstByTenantIdOrderByCreatedAtAsc(UUID tenantId);

	Optional<School> findByIdAndTenantId(UUID id, UUID tenantId);

	boolean existsByTenantId(UUID tenantId);
}

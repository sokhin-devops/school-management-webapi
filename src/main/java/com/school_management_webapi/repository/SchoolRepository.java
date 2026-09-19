package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.school_management_webapi.entity.School;

public interface SchoolRepository extends JpaRepository<School, UUID> {

	List<School> findByTenantIdOrderByCreatedAtAsc(UUID tenantId);

	Optional<School> findFirstByTenantIdOrderByCreatedAtAsc(UUID tenantId);

	Optional<School> findByIdAndTenantId(UUID id, UUID tenantId);

	Optional<School> findByTenantIdAndNameIgnoreCase(UUID tenantId, String name);

	boolean existsByIdAndTenantId(UUID id, UUID tenantId);

	@Query("SELECT s.id FROM School s WHERE s.tenant.id = :tenantId")
	List<UUID> findIdsByTenantId(@Param("tenantId") UUID tenantId);
}

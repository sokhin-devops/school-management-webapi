package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.school_management_webapi.entity.Branch;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

	List<Branch> findBySchoolIdOrderByCreatedAtAsc(UUID schoolId);

	Optional<Branch> findFirstBySchoolIdOrderByCreatedAtAsc(UUID schoolId);

	Optional<Branch> findBySchoolIdAndNameIgnoreCase(UUID schoolId, String name);

	boolean existsBySchoolId(UUID schoolId);

	@Query("SELECT b FROM Branch b WHERE b.id = :id AND b.school.tenant.id = :tenantId")
	Optional<Branch> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

	@Query("SELECT b FROM Branch b WHERE b.school.tenant.id = :tenantId ORDER BY b.createdAt ASC")
	List<Branch> findAllByTenantId(@Param("tenantId") UUID tenantId);

	/**
	 * Branch usage is counted per tenant, not per school, because
	 * {@code Plan.maxBranches} is a tenant-wide allowance.
	 */
	@Query("SELECT COUNT(b) FROM Branch b WHERE b.school.tenant.id = :tenantId")
	long countByTenantId(@Param("tenantId") UUID tenantId);
}

package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.school_management_webapi.entity.AcademicYear;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

	List<AcademicYear> findBySchoolIdOrderByStartDateDesc(UUID schoolId);

	Optional<AcademicYear> findFirstBySchoolIdOrderByCreatedAtAsc(UUID schoolId);

	boolean existsBySchoolId(UUID schoolId);

	@Query("SELECT a FROM AcademicYear a WHERE a.id = :id AND a.school.tenant.id = :tenantId")
	Optional<AcademicYear> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

	@Query("SELECT a FROM AcademicYear a WHERE a.school.tenant.id = :tenantId ORDER BY a.startDate DESC")
	List<AcademicYear> findAllByTenantId(@Param("tenantId") UUID tenantId);
}

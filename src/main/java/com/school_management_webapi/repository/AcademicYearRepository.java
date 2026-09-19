package com.school_management_webapi.repository;

import java.time.LocalDate;
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

	Optional<AcademicYear> findBySchoolIdAndNameIgnoreCase(UUID schoolId, String name);

	boolean existsBySchoolId(UUID schoolId);

	@Query("SELECT a FROM AcademicYear a WHERE a.id = :id AND a.school.tenant.id = :tenantId")
	Optional<AcademicYear> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

	@Query("SELECT a FROM AcademicYear a WHERE a.school.tenant.id = :tenantId ORDER BY a.startDate DESC")
	List<AcademicYear> findAllByTenantId(@Param("tenantId") UUID tenantId);

	/**
	 * Two ranges overlap when each starts on or before the other ends. {@code
	 * excludedId} lets an update ignore the row being edited.
	 */
	@Query("""
			SELECT a FROM AcademicYear a
			WHERE a.school.id = :schoolId
			  AND (:excludedId IS NULL OR a.id <> :excludedId)
			  AND a.startDate <= :endDate
			  AND a.endDate >= :startDate
			ORDER BY a.startDate ASC
			""")
	List<AcademicYear> findOverlapping(@Param("schoolId") UUID schoolId, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate, @Param("excludedId") UUID excludedId);
}

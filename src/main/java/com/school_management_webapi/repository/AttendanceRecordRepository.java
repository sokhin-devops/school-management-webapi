package com.school_management_webapi.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import com.school_management_webapi.entity.AttendanceRecord;
import com.school_management_webapi.entity.AttendanceStatus;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID>, JpaSpecificationExecutor<AttendanceRecord> {

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);

	List<AttendanceRecord> findByBranchIdInAndAttendanceDateBetween(Collection<UUID> branchIds,
			LocalDate from, LocalDate to);

	/**
	 * The four columns below are what identifies one sitting of one class. Session
	 * is stored as an empty string rather than null for a class taken once a day,
	 * so these stay simple equality lookups.
	 */
	List<AttendanceRecord> findByBranchIdAndClassGroupIdAndAttendanceDateAndSession(UUID branchId, UUID classGroupId,
			LocalDate attendanceDate, String session);

	void deleteByBranchIdAndClassGroupIdAndAttendanceDateAndSession(UUID branchId, UUID classGroupId,
			LocalDate attendanceDate, String session);

	/**
	 * Present against total, one row per day. Counted in the database rather than
	 * loaded and tallied here, because a term of registers is a lot of rows to
	 * bring back for two numbers.
	 */
	@Query("""
			SELECT a.attendanceDate,
			       SUM(CASE WHEN a.status = :present THEN 1 ELSE 0 END),
			       COUNT(a)
			FROM AttendanceRecord a
			WHERE a.branchId IN :branchIds AND a.attendanceDate BETWEEN :from AND :to
			GROUP BY a.attendanceDate
			ORDER BY a.attendanceDate
			""")
	List<Object[]> summariseByDay(@Param("branchIds") Collection<UUID> branchIds,
			@Param("from") LocalDate from, @Param("to") LocalDate to,
			@Param("present") AttendanceStatus present);

	List<AttendanceRecord> findByBranchIdIn(Collection<UUID> branchIds);
}

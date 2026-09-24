package com.school_management_webapi.specification;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.school_management_webapi.entity.AttendanceRecord;
import com.school_management_webapi.entity.AttendanceStatus;

public final class AttendanceRecordSpecification {

	private AttendanceRecordSpecification() {
	}

	/**
	 * @param allowedBranchIds the branches the caller's tenant owns. An empty
	 *                         collection matches nothing, which is the correct
	 *                         answer for a tenant with no branch yet.
	 */
	public static Specification<AttendanceRecord> filterBy(Collection<UUID> allowedBranchIds,
			UUID branchId,
			AttendanceStatus status,
			UUID classGroupId,
			UUID studentId,
			LocalDate attendanceDateFrom,
			LocalDate attendanceDateTo,
			String search) {
		return (root, query, cb) -> {
			if (allowedBranchIds.isEmpty()) {
				return cb.disjunction();
			}

			var predicate = cb.and(root.get("branchId").in(allowedBranchIds));

			if (branchId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("branchId"), branchId));
			}
			if (status != null) {
				predicate = cb.and(predicate, cb.equal(root.get("status"), status));
			}
			if (classGroupId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("classGroupId"), classGroupId));
			}
			if (studentId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("studentId"), studentId));
			}
			if (attendanceDateFrom != null) {
				predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("attendanceDate"), attendanceDateFrom));
			}
			if (attendanceDateTo != null) {
				predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("attendanceDate"), attendanceDateTo));
			}
			if (search != null && !search.isBlank()) {
				String pattern = "%" + search.trim().toLowerCase() + "%";
				predicate = cb.and(predicate, cb.or(
						cb.like(cb.lower(cb.coalesce(root.get("note"), "")), pattern)));
			}

			return predicate;
		};
	}
}

package com.school_management_webapi.specification;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.entity.Teacher;

public final class TeacherSpecification {

	private TeacherSpecification() {
	}

	/**
	 * @param allowedBranchIds the branches the caller's tenant owns. An empty
	 *                         collection matches nothing, which is the correct
	 *                         answer for a tenant with no branch yet.
	 */
	public static Specification<Teacher> filterBy(Collection<UUID> allowedBranchIds,
			UUID branchId,
			RecordStatus status,
			Gender gender,
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
			if (gender != null) {
				predicate = cb.and(predicate, cb.equal(root.get("gender"), gender));
			}
			if (search != null && !search.isBlank()) {
				String pattern = "%" + search.trim().toLowerCase() + "%";
				predicate = cb.and(predicate, cb.or(
						cb.like(cb.lower(cb.coalesce(root.get("employeeNumber"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("firstName"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("lastName"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("email"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("phone"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("department"), "")), pattern)));
			}

			return predicate;
		};
	}
}

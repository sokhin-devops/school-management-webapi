package com.school_management_webapi.specification;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.school_management_webapi.entity.Assessment;
import com.school_management_webapi.entity.AssessmentType;

public final class AssessmentSpecification {

	private AssessmentSpecification() {
	}

	/**
	 * @param allowedBranchIds the branches the caller's tenant owns. An empty
	 *                         collection matches nothing, which is the correct
	 *                         answer for a tenant with no branch yet.
	 */
	public static Specification<Assessment> filterBy(Collection<UUID> allowedBranchIds,
			UUID branchId,
			AssessmentType type,
			UUID classGroupId,
			UUID subjectId,
			UUID academicYearId,
			LocalDate assessedOnFrom,
			LocalDate assessedOnTo,
			String search) {
		return (root, query, cb) -> {
			if (allowedBranchIds.isEmpty()) {
				return cb.disjunction();
			}

			var predicate = cb.and(root.get("branchId").in(allowedBranchIds));

			if (branchId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("branchId"), branchId));
			}
			if (type != null) {
				predicate = cb.and(predicate, cb.equal(root.get("type"), type));
			}
			if (classGroupId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("classGroupId"), classGroupId));
			}
			if (subjectId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("subjectId"), subjectId));
			}
			if (academicYearId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("academicYearId"), academicYearId));
			}
			if (assessedOnFrom != null) {
				predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("assessedOn"), assessedOnFrom));
			}
			if (assessedOnTo != null) {
				predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("assessedOn"), assessedOnTo));
			}
			if (search != null && !search.isBlank()) {
				String pattern = "%" + search.trim().toLowerCase() + "%";
				predicate = cb.and(predicate, cb.or(
						cb.like(cb.lower(cb.coalesce(root.get("name"), "")), pattern)));
			}

			return predicate;
		};
	}
}

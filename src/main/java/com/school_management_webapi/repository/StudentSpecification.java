package com.school_management_webapi.repository;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.Student;
import com.school_management_webapi.entity.StudentStatus;

public final class StudentSpecification {

	private StudentSpecification() {
	}

	/**
	 * @param allowedSchoolIds the schools the caller's tenant owns. An empty
	 *                         collection matches nothing, which is the correct
	 *                         answer for a tenant that has no school yet.
	 */
	public static Specification<Student> filterBy(Collection<UUID> allowedSchoolIds, UUID schoolId,
			StudentStatus status, Gender gender, String search) {
		return (root, query, cb) -> {
			if (allowedSchoolIds.isEmpty()) {
				return cb.disjunction();
			}

			var predicate = cb.and(root.get("schoolId").in(allowedSchoolIds));

			if (schoolId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("schoolId"), schoolId));
			}
			if (status != null) {
				predicate = cb.and(predicate, cb.equal(root.get("status"), status));
			}
			if (gender != null) {
				predicate = cb.and(predicate, cb.equal(root.get("gender"), gender));
			}
			if (search != null && !search.isBlank()) {
				String pattern = "%" + search.trim().toLowerCase() + "%";
				var searchPredicate = cb.or(
						cb.like(cb.lower(root.get("studentCode")), pattern),
						cb.like(cb.lower(root.get("firstName")), pattern),
						cb.like(cb.lower(root.get("lastName")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("preferredName"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("email"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("phone"), "")), pattern));
				predicate = cb.and(predicate, searchPredicate);
			}

			return predicate;
		};
	}
}

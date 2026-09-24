package com.school_management_webapi.specification;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.entity.Room;

public final class RoomSpecification {

	private RoomSpecification() {
	}

	/**
	 * @param allowedBranchIds the branches the caller's tenant owns. An empty
	 *                         collection matches nothing, which is the correct
	 *                         answer for a tenant with no branch yet.
	 */
	public static Specification<Room> filterBy(Collection<UUID> allowedBranchIds,
			UUID branchId,
			RecordStatus status,
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
			if (search != null && !search.isBlank()) {
				String pattern = "%" + search.trim().toLowerCase() + "%";
				predicate = cb.and(predicate, cb.or(
						cb.like(cb.lower(cb.coalesce(root.get("name"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("code"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("building"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("kind"), "")), pattern)));
			}

			return predicate;
		};
	}
}

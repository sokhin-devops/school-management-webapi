package com.school_management_webapi.specification;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.school_management_webapi.entity.Payment;
import com.school_management_webapi.entity.PaymentMethod;
import com.school_management_webapi.entity.PaymentStatus;

public final class PaymentSpecification {

	private PaymentSpecification() {
	}

	/**
	 * @param allowedBranchIds the branches the caller's tenant owns. An empty
	 *                         collection matches nothing, which is the correct
	 *                         answer for a tenant with no branch yet.
	 */
	public static Specification<Payment> filterBy(Collection<UUID> allowedBranchIds,
			UUID branchId,
			PaymentStatus status,
			PaymentMethod method,
			UUID feeId,
			UUID studentId,
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
			if (method != null) {
				predicate = cb.and(predicate, cb.equal(root.get("method"), method));
			}
			if (feeId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("feeId"), feeId));
			}
			if (studentId != null) {
				predicate = cb.and(predicate, cb.equal(root.get("studentId"), studentId));
			}
			if (search != null && !search.isBlank()) {
				String pattern = "%" + search.trim().toLowerCase() + "%";
				predicate = cb.and(predicate, cb.or(
						cb.like(cb.lower(cb.coalesce(root.get("reference"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("payerName"), "")), pattern),
						cb.like(cb.lower(cb.coalesce(root.get("notes"), "")), pattern)));
			}

			return predicate;
		};
	}
}

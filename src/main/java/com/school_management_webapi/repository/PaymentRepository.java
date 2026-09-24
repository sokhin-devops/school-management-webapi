package com.school_management_webapi.repository;

import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import com.school_management_webapi.entity.Payment;
import com.school_management_webapi.entity.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

	boolean existsByReferenceAndBranchId(String reference, UUID branchId);

	Optional<Payment> findByReferenceAndBranchId(String reference, UUID branchId);

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);

	@Query("""
			SELECT COALESCE(SUM(p.amount), 0)
			FROM Payment p
			WHERE p.branchId IN :branchIds AND p.status = :status AND p.paidOn BETWEEN :from AND :to
			""")
	BigDecimal sumAmount(@Param("branchIds") Collection<UUID> branchIds, @Param("status") PaymentStatus status,
			@Param("from") LocalDate from, @Param("to") LocalDate to);
}

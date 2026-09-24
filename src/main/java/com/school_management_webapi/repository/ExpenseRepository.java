package com.school_management_webapi.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import com.school_management_webapi.entity.Expense;
import com.school_management_webapi.entity.ExpenseStatus;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);

	@Query("""
			SELECT COALESCE(SUM(e.amount), 0)
			FROM Expense e
			WHERE e.branchId IN :branchIds AND e.status = :status AND e.spentOn BETWEEN :from AND :to
			""")
	BigDecimal sumAmount(@Param("branchIds") Collection<UUID> branchIds, @Param("status") ExpenseStatus status,
			@Param("from") LocalDate from, @Param("to") LocalDate to);
}

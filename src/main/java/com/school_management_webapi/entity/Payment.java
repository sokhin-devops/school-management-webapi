package com.school_management_webapi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Money received against a fee. 42-payments.md: amount, date, method,
 * status, payer and the student it settles for.
 */
@Entity
@Table(name = "payments",
		uniqueConstraints = @UniqueConstraint(name = "uk_payments_branch_id_reference", columnNames = { "branch_id", "reference" }))
@SQLDelete(sql = "UPDATE payments SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	/** Unique within the branch; what a receipt is looked up by. */
	@Column(nullable = false, length = 50)
	private String reference;

	@Column(name = "fee_id", nullable = false)
	private UUID feeId;

	@Column(name = "student_id", nullable = false)
	private UUID studentId;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(name = "paid_on", nullable = false)
	private LocalDate paidOn;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod method;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(name = "payer_name", length = 150)
	private String payerName;

	@Column(length = 500, columnDefinition = "TEXT")
	private String notes;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}

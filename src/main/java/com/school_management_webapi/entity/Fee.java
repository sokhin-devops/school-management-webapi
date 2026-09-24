package com.school_management_webapi.entity;

import java.math.BigDecimal;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A charge the branch can bill for. 41-fees.md: tuition, registration,
 * transport, laboratory, materials - the category is free text because the
 * list differs by school and grows over time.
 */
@Entity
@Table(name = "fees")
@SQLDelete(sql = "UPDATE fees SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fee {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	/** The year this fee is billed for. Optional: some charges are one-off. */
	@Column(name = "academic_year_id")
	private UUID academicYearId;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 80)
	private String category;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(length = 500, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RecordStatus status;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}

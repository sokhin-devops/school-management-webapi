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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One piece of assessed work set for a class. 31-exams-and-grades.md:
 * grading scales differ by school, so the raw score is what is stored and
 * the scale is applied when it is displayed.
 */
@Entity
@Table(name = "assessments")
@SQLDelete(sql = "UPDATE assessments SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "academic_year_id")
	private UUID academicYearId;

	@Column(name = "class_group_id", nullable = false)
	private UUID classGroupId;

	@Column(name = "subject_id", nullable = false)
	private UUID subjectId;

	@Column(nullable = false, length = 150)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AssessmentType type;

	@Column(name = "assessed_on", nullable = false)
	private LocalDate assessedOn;

	/** What a perfect paper earns. The scores recorded against it cannot exceed it. */
	@Column(name = "max_score", nullable = false)
	private Integer maxScore;

	/** Recomputed whenever scores are saved; never sent in by a client. */
	@Column(name = "average_score", precision = 6, scale = 2)
	private BigDecimal averageScore;

	/** True once at least one score has been recorded. Also maintained by the score endpoint. */
	private Boolean graded;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}

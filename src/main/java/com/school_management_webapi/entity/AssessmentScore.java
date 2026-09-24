package com.school_management_webapi.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * One student's mark for one assessment.
 *
 * Carries no branch of its own: it belongs to an assessment, and that is what
 * decides which tenant may see it. It is not soft-deleted either - a mark sheet
 * is replaced wholesale, so a removed row means the student was not marked,
 * which is a different thing from an archived record.
 */
@Entity
@Table(name = "assessment_scores",
		uniqueConstraints = @UniqueConstraint(name = "uk_assessment_scores_assessment_id_student_id",
				columnNames = { "assessment_id", "student_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentScore {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "assessment_id", nullable = false)
	private UUID assessmentId;

	@Column(name = "student_id", nullable = false)
	private UUID studentId;

	/** Absent means the student has not been marked yet, which is not the same as zero. */
	@Column(name = "score", precision = 6, scale = 2)
	private BigDecimal score;

	@Column(name = "remark", length = 255)
	private String remark;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}

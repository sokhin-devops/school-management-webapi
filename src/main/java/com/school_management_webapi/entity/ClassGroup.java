package com.school_management_webapi.entity;

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
 * One taught group of students. 23-classes.md: the structure above it
 * differs by school type - Grade 1 to Class 1-A, Programme to Year 2 to
 * Section A, Course to Intermediate to Batch 03 - so the parents are
 * optional links rather than a fixed hierarchy, and a class may nest under
 * another through parentClassId.
 */
@Entity
@Table(name = "class_groups",
		uniqueConstraints = @UniqueConstraint(name = "uk_class_groups_branch_id_code", columnNames = { "branch_id", "code" }))
@SQLDelete(sql = "UPDATE class_groups SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassGroup {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "academic_year_id", nullable = false)
	private UUID academicYearId;

	@Column(name = "program_id")
	private UUID programId;

	@Column(name = "level_id")
	private UUID levelId;

	/** The class this one is a section of, for the deeper structures above. */
	@Column(name = "parent_class_id")
	private UUID parentClassId;

	@Column(name = "homeroom_teacher_id")
	private UUID homeroomTeacherId;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 30)
	private String code;

	private Integer capacity;

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

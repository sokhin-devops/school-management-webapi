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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One stage of a programme. 22-levels.md: the label is configurable -
 * Grade, Year, Level, Form - so the word is stored with the row rather
 * than assumed by the code that displays it.
 */
@Entity
@Table(name = "levels")
@SQLDelete(sql = "UPDATE levels SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Level {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	/** Optional: a branch that does not use programmes still has levels. */
	@Column(name = "program_id")
	private UUID programId;

	@Column(nullable = false, length = 100)
	private String name;

	/** What a level of this kind is called here: Grade, Year, Level, Form. */
	@Column(name = "display_label", length = 50)
	private String displayLabel;

	/** Where this level sits in the sequence, which is the order it is listed in. */
	@Column(name = "sort_order")
	private Integer order;

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

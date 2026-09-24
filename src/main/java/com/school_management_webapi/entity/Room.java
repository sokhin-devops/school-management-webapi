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
 * A teaching space. 26-rooms.md: a room is an independent resource, not a
 * property of a class - the same class may sit in different rooms at
 * different times - so nothing here points back at a class.
 */
@Entity
@Table(name = "rooms",
		uniqueConstraints = @UniqueConstraint(name = "uk_rooms_branch_id_code", columnNames = { "branch_id", "code" }))
@SQLDelete(sql = "UPDATE rooms SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 30)
	private String code;

	@Column(length = 150)
	private String building;

	/** Classroom, Laboratory, Studio, Study - free text, because it varies by school. */
	@Column(length = 50)
	private String kind;

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

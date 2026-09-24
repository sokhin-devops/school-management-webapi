package com.school_management_webapi.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A parent or guardian, and the students they are responsible for.
 * A parent carries no code of their own - nothing outside the system
 * numbers them - so they are identified by id alone.
 */
@Entity
@Table(name = "parents")
@SQLDelete(sql = "UPDATE parents SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parent {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	/** Father, Mother, Guardian - free text, because families are not an enum. */
	@Column(nullable = false, length = 50)
	private String relationship;

	@Column(nullable = false, length = 255)
	private String email;

	/** Required: this is the number the school calls first. */
	@Column(nullable = false, length = 30)
	private String phone;

	/** The students in this parent's care. Replaced wholesale on every save. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "parent_students", joinColumns = @JoinColumn(name = "parent_id"))
	@Column(name = "student_id", nullable = false)
	@Builder.Default
	private List<UUID> studentIds = new ArrayList<>();

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

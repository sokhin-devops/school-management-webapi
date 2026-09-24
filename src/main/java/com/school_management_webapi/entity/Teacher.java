package com.school_management_webapi.entity;

import java.time.LocalDate;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A member of teaching staff. 11-people.md keeps students, teachers and
 * parents on one conceptual Person, but each carries a different set of
 * facts, so each gets its own table rather than a wide shared one with
 * two thirds of the columns null.
 */
@Entity
@Table(name = "teachers",
		uniqueConstraints = @UniqueConstraint(name = "uk_teachers_branch_id_employee_number", columnNames = { "branch_id", "employee_number" }))
@SQLDelete(sql = "UPDATE teachers SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	/** Unique within the branch; the record's identity on a payroll or a timetable. */
	@Column(name = "employee_number", nullable = false, length = 40)
	private String employeeNumber;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(name = "date_of_birth")
	private LocalDate dateOfBirth;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(length = 30)
	private String phone;

	@Column(nullable = false, length = 100)
	private String department;

	@Column(name = "hire_date")
	private LocalDate hireDate;

	/** The subjects this teacher takes. Replaced wholesale on every save. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "teacher_subjects", joinColumns = @JoinColumn(name = "teacher_id"))
	@Column(name = "subject_id", nullable = false)
	@Builder.Default
	private List<UUID> subjectIds = new ArrayList<>();

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

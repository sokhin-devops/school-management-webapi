package com.school_management_webapi.entity;

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
 * Core student profile. Satellite data (addresses, parents, enrollments,
 * documents, medical, attendance, fees, payments) lives in their own tables -
 * see docs/todo/student.md for the rest of the module roadmap.
 */
@Entity
@Table(name = "students", uniqueConstraints = @UniqueConstraint(name = "uk_students_school_id_student_code", columnNames = { "school_id", "student_code" }))
@SQLDelete(sql = "UPDATE students SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "school_id", nullable = false)
	private UUID schoolId;

	/**
	 * Unique per school, not globally - see the table-level constraint. A legacy
	 * single-column unique index on student_code has to be dropped by hand because
	 * ddl-auto=update never removes constraints; see docs/plain/3-school-onboarding.md.
	 */
	@Column(name = "student_code", nullable = false)
	private String studentCode;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name = "middle_name")
	private String middleName;

	@Column(name = "last_name", nullable = false)
	private String lastName;

	@Column(name = "preferred_name")
	private String preferredName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Gender gender;

	@Column(name = "date_of_birth", nullable = false)
	private LocalDate dateOfBirth;

	private String nationality;

	@Column(name = "national_id")
	private String nationalId;

	@Column(name = "photo_url")
	private String photoUrl;

	private String email;

	private String phone;

	@Column(name = "admission_date", nullable = false)
	private LocalDate admissionDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StudentStatus status;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}

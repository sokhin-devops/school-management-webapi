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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One student, one session, one mark. 30-attendance.md: a class may be
 * taken more than once a day, so the session is part of what identifies a
 * record rather than the date alone.
 */
@Entity
@Table(name = "attendance_records")
@SQLDelete(sql = "UPDATE attendance_records SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "class_group_id", nullable = false)
	private UUID classGroupId;

	@Column(name = "student_id", nullable = false)
	private UUID studentId;

	@Column(name = "attendance_date", nullable = false)
	private LocalDate attendanceDate;

	/** Which sitting of the day this is. Blank means the class is taken once. */
	@Column(length = 50)
	private String session;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AttendanceStatus status;

	@Column(length = 255)
	private String note;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}

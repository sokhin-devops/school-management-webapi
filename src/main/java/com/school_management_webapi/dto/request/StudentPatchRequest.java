package com.school_management_webapi.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.StudentStatus;

import jakarta.validation.constraints.Email;

/**
 * Partial update payload for PATCH /api/v1/students/{id}. Every field is
 * optional - only non-null fields are applied.
 */
public record StudentPatchRequest(
		UUID schoolId,

		UUID branchId,

		UUID classGroupId,

		String studentCode,
		String firstName,
		String middleName,
		String lastName,
		String preferredName,
		Gender gender,
		LocalDate dateOfBirth,
		String nationality,
		String nationalId,
		String photoUrl,
		@Email(message = "email must be a valid email address") String email,
		String phone,
		LocalDate admissionDate,
		StudentStatus status) {
}

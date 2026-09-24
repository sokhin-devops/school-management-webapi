package com.school_management_webapi.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.StudentStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

/** Full replacement payload for PUT /api/v1/students/{id}. */
public record StudentUpdateRequest(
		@NotNull(message = "schoolId is required") UUID schoolId,

		UUID branchId,

		UUID classGroupId,

		@NotBlank(message = "studentCode is required") String studentCode,

		@NotBlank(message = "firstName is required") String firstName,

		String middleName,

		@NotBlank(message = "lastName is required") String lastName,

		String preferredName,

		@NotNull(message = "gender is required") Gender gender,

		@NotNull(message = "dateOfBirth is required") @Past(message = "dateOfBirth must be in the past") LocalDate dateOfBirth,

		String nationality,

		String nationalId,

		String photoUrl,

		@Email(message = "email must be a valid email address") String email,

		String phone,

		@NotNull(message = "admissionDate is required") @PastOrPresent(message = "admissionDate cannot be in the future") LocalDate admissionDate,

		@NotNull(message = "status is required") StudentStatus status) {
}

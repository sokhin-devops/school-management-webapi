package com.school_management_webapi.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.StudentStatus;

public record StudentResponse(
		UUID id,
		UUID schoolId,
		UUID branchId,
		UUID classGroupId,
		String studentCode,
		String firstName,
		String middleName,
		String lastName,
		String preferredName,
		String fullName,
		Gender gender,
		LocalDate dateOfBirth,
		String nationality,
		String nationalId,
		String photoUrl,
		String email,
		String phone,
		LocalDate admissionDate,
		StudentStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

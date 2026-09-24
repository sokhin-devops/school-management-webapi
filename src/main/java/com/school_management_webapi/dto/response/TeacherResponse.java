package com.school_management_webapi.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.RecordStatus;

public record TeacherResponse(
		UUID id,
		UUID branchId,
		String employeeNumber,
		String firstName,
		String lastName,
		Gender gender,
		LocalDate dateOfBirth,
		String email,
		String phone,
		String department,
		LocalDate hireDate,
		List<UUID> subjectIds,
		RecordStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

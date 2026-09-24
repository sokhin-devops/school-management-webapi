package com.school_management_webapi.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** A partial change. Every field is optional; the ones left out keep their current value. */
public record TeacherPatchRequest(
		UUID branchId,

		@Size(max = 40, message = "employeeNumber must be 40 characters or fewer") String employeeNumber,

		@Size(max = 100, message = "firstName must be 100 characters or fewer") String firstName,

		@Size(max = 100, message = "lastName must be 100 characters or fewer") String lastName,

		Gender gender,

		LocalDate dateOfBirth,

		@Size(max = 255, message = "email must be 255 characters or fewer") @Email(message = "email must be a valid email address") String email,

		@Size(max = 30, message = "phone must be 30 characters or fewer") String phone,

		@Size(max = 100, message = "department must be 100 characters or fewer") String department,

		LocalDate hireDate,

		List<UUID> subjectIds,

		RecordStatus status) {
}

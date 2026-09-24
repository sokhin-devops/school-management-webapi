package com.school_management_webapi.dto.request;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The fields needed to open a new record. */
public record ParentCreateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		@NotBlank(message = "firstName is required") @Size(max = 100, message = "firstName must be 100 characters or fewer") String firstName,

		@NotBlank(message = "lastName is required") @Size(max = 100, message = "lastName must be 100 characters or fewer") String lastName,

		@NotBlank(message = "relationship is required") @Size(max = 50, message = "relationship must be 50 characters or fewer") String relationship,

		@NotBlank(message = "email is required") @Size(max = 255, message = "email must be 255 characters or fewer") @Email(message = "email must be a valid email address") String email,

		@NotBlank(message = "phone is required") @Size(max = 30, message = "phone must be 30 characters or fewer") String phone,

		List<UUID> studentIds,

		@NotNull(message = "status is required") RecordStatus status) {
}

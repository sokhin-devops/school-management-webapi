package com.school_management_webapi.dto.request;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** A partial change. Every field is optional; the ones left out keep their current value. */
public record ParentPatchRequest(
		UUID branchId,

		@Size(max = 100, message = "firstName must be 100 characters or fewer") String firstName,

		@Size(max = 100, message = "lastName must be 100 characters or fewer") String lastName,

		@Size(max = 50, message = "relationship must be 50 characters or fewer") String relationship,

		@Size(max = 255, message = "email must be 255 characters or fewer") @Email(message = "email must be a valid email address") String email,

		@Size(max = 30, message = "phone must be 30 characters or fewer") String phone,

		List<UUID> studentIds,

		RecordStatus status) {
}

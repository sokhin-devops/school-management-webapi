package com.school_management_webapi.dto.request;

import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The fields needed to open a new record. */
public record SubjectCreateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		@NotBlank(message = "name is required") @Size(max = 150, message = "name must be 150 characters or fewer") String name,

		@NotBlank(message = "code is required") @Size(max = 30, message = "code must be 30 characters or fewer") String code,

		@Size(max = 500, message = "description must be 500 characters or fewer") String description,

		@NotNull(message = "status is required") RecordStatus status) {
}

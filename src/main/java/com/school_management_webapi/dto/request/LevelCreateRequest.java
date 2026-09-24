package com.school_management_webapi.dto.request;

import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The fields needed to open a new record. */
public record LevelCreateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		UUID programId,

		@NotBlank(message = "name is required") @Size(max = 100, message = "name must be 100 characters or fewer") String name,

		@Size(max = 50, message = "displayLabel must be 50 characters or fewer") String displayLabel,

		@Min(value = 1, message = "order must be 1 or more") @Max(value = 100, message = "order must be 100 or less") Integer order,

		@NotNull(message = "status is required") RecordStatus status) {
}

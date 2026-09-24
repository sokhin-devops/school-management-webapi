package com.school_management_webapi.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A full replacement: every field is written, so every required one must be present. */
public record FeeUpdateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		UUID academicYearId,

		@NotBlank(message = "name is required") @Size(max = 150, message = "name must be 150 characters or fewer") String name,

		@NotBlank(message = "category is required") @Size(max = 80, message = "category must be 80 characters or fewer") String category,

		@NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be 0.01 or more") BigDecimal amount,

		@Size(max = 500, message = "description must be 500 characters or fewer") String description,

		@NotNull(message = "status is required") RecordStatus status) {
}

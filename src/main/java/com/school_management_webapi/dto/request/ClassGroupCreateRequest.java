package com.school_management_webapi.dto.request;

import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The fields needed to open a new record. */
public record ClassGroupCreateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		@NotNull(message = "academicYearId is required") UUID academicYearId,

		UUID programId,

		UUID levelId,

		UUID parentClassId,

		UUID homeroomTeacherId,

		@NotBlank(message = "name is required") @Size(max = 150, message = "name must be 150 characters or fewer") String name,

		@NotBlank(message = "code is required") @Size(max = 30, message = "code must be 30 characters or fewer") String code,

		@Min(value = 1, message = "capacity must be 1 or more") @Max(value = 500, message = "capacity must be 500 or less") Integer capacity,

		@NotNull(message = "status is required") RecordStatus status) {
}

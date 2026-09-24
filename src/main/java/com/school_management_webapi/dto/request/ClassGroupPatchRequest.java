package com.school_management_webapi.dto.request;

import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** A partial change. Every field is optional; the ones left out keep their current value. */
public record ClassGroupPatchRequest(
		UUID branchId,

		UUID academicYearId,

		UUID programId,

		UUID levelId,

		UUID parentClassId,

		UUID homeroomTeacherId,

		@Size(max = 150, message = "name must be 150 characters or fewer") String name,

		@Size(max = 30, message = "code must be 30 characters or fewer") String code,

		@Min(value = 1, message = "capacity must be 1 or more") @Max(value = 500, message = "capacity must be 500 or less") Integer capacity,

		RecordStatus status) {
}

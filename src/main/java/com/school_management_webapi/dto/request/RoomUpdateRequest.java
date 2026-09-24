package com.school_management_webapi.dto.request;

import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A full replacement: every field is written, so every required one must be present. */
public record RoomUpdateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		@NotBlank(message = "name is required") @Size(max = 150, message = "name must be 150 characters or fewer") String name,

		@NotBlank(message = "code is required") @Size(max = 30, message = "code must be 30 characters or fewer") String code,

		@Size(max = 150, message = "building must be 150 characters or fewer") String building,

		@Size(max = 50, message = "kind must be 50 characters or fewer") String kind,

		@Min(value = 1, message = "capacity must be 1 or more") @Max(value = 1000, message = "capacity must be 1000 or less") Integer capacity,

		@NotNull(message = "status is required") RecordStatus status) {
}

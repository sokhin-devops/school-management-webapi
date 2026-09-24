package com.school_management_webapi.dto.request;

import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.Size;

/** A partial change. Every field is optional; the ones left out keep their current value. */
public record SubjectPatchRequest(
		UUID branchId,

		@Size(max = 150, message = "name must be 150 characters or fewer") String name,

		@Size(max = 30, message = "code must be 30 characters or fewer") String code,

		@Size(max = 500, message = "description must be 500 characters or fewer") String description,

		RecordStatus status) {
}

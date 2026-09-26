package com.school_management_webapi.dto.request;

import java.util.UUID;

import com.school_management_webapi.entity.BranchStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BranchRequest(
		@NotNull(message = "schoolId is required") UUID schoolId,

		@NotBlank(message = "name is required") @Size(max = 150, message = "name must be at most 150 characters") String name,

		@NotBlank(message = "address is required") @Size(max = 1000, message = "address must be at most 1000 characters") String address,

		@Size(max = 30, message = "phone must be at most 30 characters") String phone,

		boolean mainBranch,

		/** Optional: a create with none is active, an update with none keeps what it had. */
		BranchStatus status) {
}

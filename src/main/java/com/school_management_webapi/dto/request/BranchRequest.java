package com.school_management_webapi.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BranchRequest(
		@NotNull(message = "schoolId is required") UUID schoolId,

		@NotBlank(message = "name is required") String name,

		@NotBlank(message = "address is required") String address,

		String phone,

		boolean mainBranch) {
}

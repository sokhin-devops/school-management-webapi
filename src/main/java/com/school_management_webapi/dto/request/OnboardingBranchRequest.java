package com.school_management_webapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OnboardingBranchRequest(
		@NotBlank(message = "name is required") String name,

		@NotBlank(message = "address is required") String address,

		String phone) {
}

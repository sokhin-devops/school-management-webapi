package com.school_management_webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OnboardingBranchRequest(
		@NotBlank(message = "name is required") @Size(max = 150, message = "name must be at most 150 characters") String name,

		@NotBlank(message = "address is required") @Size(max = 1000, message = "address must be at most 1000 characters") String address,

		@Size(max = 30, message = "phone must be at most 30 characters") String phone) {
}

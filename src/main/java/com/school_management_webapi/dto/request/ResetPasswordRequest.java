package com.school_management_webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
		@NotBlank(message = "token is required") String token,

		@NotBlank(message = "password is required") @Size(min = 8, message = "password must be at least 8 characters") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "password must contain at least one uppercase letter, one lowercase letter and one digit") String password,

		@NotBlank(message = "confirmPassword is required") String confirmPassword) {
}

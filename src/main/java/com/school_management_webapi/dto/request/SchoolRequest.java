package com.school_management_webapi.dto.request;

import com.school_management_webapi.entity.SchoolType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SchoolRequest(
		@NotBlank(message = "name is required") @Size(max = 150, message = "name must be at most 150 characters") String name,

		@NotNull(message = "type is required") SchoolType type,

		@NotBlank(message = "email is required") @Email(message = "email must be a valid email address") @Size(max = 255, message = "email must be at most 255 characters") String email,

		@NotBlank(message = "phone is required") @Size(max = 30, message = "phone must be at most 30 characters") String phone,

		@NotBlank(message = "address is required") @Size(max = 1000, message = "address must be at most 1000 characters") String address) {
}

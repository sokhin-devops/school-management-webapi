package com.school_management_webapi.dto.request;

import com.school_management_webapi.entity.SchoolType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SchoolRequest(
		@NotBlank(message = "name is required") String name,

		@NotNull(message = "type is required") SchoolType type,

		@NotBlank(message = "email is required") @Email(message = "email must be a valid email address") String email,

		@NotBlank(message = "phone is required") String phone,

		@NotBlank(message = "address is required") String address) {
}

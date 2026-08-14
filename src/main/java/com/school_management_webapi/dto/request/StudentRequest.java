package com.school_management_webapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentRequest(

		@NotBlank(message = "First name is required")
		@Size(max = 100, message = "First name must not exceed 100 characters")
		String firstName,

		@NotBlank(message = "Last name is required")
		@Size(max = 100, message = "Last name must not exceed 100 characters")
		String lastName,

		@NotBlank(message = "Email is required")
		@Email(message = "Email must be a valid email address")
		@Size(max = 150, message = "Email must not exceed 150 characters")
		String email,

		@Pattern(regexp = "^$|^[+0-9 ()-]{7,20}$", message = "Phone number is invalid")
		String phoneNumber,

		@NotNull(message = "Date of birth is required")
		@Past(message = "Date of birth must be in the past")
		LocalDate dateOfBirth,

		@Size(max = 10, message = "Gender must not exceed 10 characters")
		String gender,

		@Size(max = 255, message = "Address must not exceed 255 characters")
		String address,

		@NotBlank(message = "Admission number is required")
		@Size(max = 50, message = "Admission number must not exceed 50 characters")
		String admissionNumber,

		@NotNull(message = "Admission date is required")
		LocalDate admissionDate
) {
}

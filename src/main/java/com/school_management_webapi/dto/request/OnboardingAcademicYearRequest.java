package com.school_management_webapi.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OnboardingAcademicYearRequest(
		@NotBlank(message = "name is required") String name,

		@NotNull(message = "startDate is required") LocalDate startDate,

		@NotNull(message = "endDate is required") LocalDate endDate) {
}

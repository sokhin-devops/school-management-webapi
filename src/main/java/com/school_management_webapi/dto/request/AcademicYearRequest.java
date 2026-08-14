package com.school_management_webapi.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcademicYearRequest(
		@NotNull(message = "schoolId is required") UUID schoolId,

		@NotBlank(message = "name is required") String name,

		@NotNull(message = "startDate is required") LocalDate startDate,

		@NotNull(message = "endDate is required") LocalDate endDate,

		boolean current) {
}

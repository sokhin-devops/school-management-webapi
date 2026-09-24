package com.school_management_webapi.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.AssessmentType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The fields needed to open a new record. */
public record AssessmentCreateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		UUID academicYearId,

		@NotNull(message = "classGroupId is required") UUID classGroupId,

		@NotNull(message = "subjectId is required") UUID subjectId,

		@NotBlank(message = "name is required") @Size(max = 150, message = "name must be 150 characters or fewer") String name,

		@NotNull(message = "type is required") AssessmentType type,

		@NotNull(message = "assessedOn is required") LocalDate assessedOn,

		@NotNull(message = "maxScore is required") @Min(value = 1, message = "maxScore must be 1 or more") @Max(value = 1000, message = "maxScore must be 1000 or less") Integer maxScore) {
}

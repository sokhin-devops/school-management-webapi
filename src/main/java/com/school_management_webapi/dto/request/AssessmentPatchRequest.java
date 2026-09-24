package com.school_management_webapi.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.AssessmentType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** A partial change. Every field is optional; the ones left out keep their current value. */
public record AssessmentPatchRequest(
		UUID branchId,

		UUID academicYearId,

		UUID classGroupId,

		UUID subjectId,

		@Size(max = 150, message = "name must be 150 characters or fewer") String name,

		AssessmentType type,

		LocalDate assessedOn,

		@Min(value = 1, message = "maxScore must be 1 or more") @Max(value = 1000, message = "maxScore must be 1000 or less") Integer maxScore) {
}

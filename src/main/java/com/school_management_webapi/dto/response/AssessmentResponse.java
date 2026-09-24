package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.AssessmentType;

public record AssessmentResponse(
		UUID id,
		UUID branchId,
		UUID academicYearId,
		UUID classGroupId,
		UUID subjectId,
		String name,
		AssessmentType type,
		LocalDate assessedOn,
		Integer maxScore,
		BigDecimal averageScore,
		Boolean graded,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

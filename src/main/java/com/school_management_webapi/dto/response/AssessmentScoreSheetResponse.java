package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** The sheet plus the two numbers derived from it, so a client needs one call. */
public record AssessmentScoreSheetResponse(
		UUID assessmentId,
		Integer maxScore,
		BigDecimal averageScore,
		Boolean graded,
		List<AssessmentScoreResponse> scores) {
}

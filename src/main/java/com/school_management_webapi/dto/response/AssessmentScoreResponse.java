package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AssessmentScoreResponse(
		UUID id,
		UUID studentId,
		BigDecimal score,
		String remark) {
}

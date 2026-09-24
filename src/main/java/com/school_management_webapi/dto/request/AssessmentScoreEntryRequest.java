package com.school_management_webapi.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** One row of a mark sheet. A null score means the student was not marked. */
public record AssessmentScoreEntryRequest(
		@NotNull(message = "studentId is required") UUID studentId,

		@DecimalMin(value = "0", message = "score cannot be negative") BigDecimal score,

		@Size(max = 255, message = "remark must be 255 characters or fewer") String remark) {
}

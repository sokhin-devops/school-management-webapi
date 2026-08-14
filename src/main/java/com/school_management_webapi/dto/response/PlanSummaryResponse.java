package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanSummaryResponse(
		UUID id,
		String code,
		String name,
		BigDecimal priceMonthly,
		BigDecimal priceYearly,
		String currency,
		Integer maxStudents,
		Integer maxTeachers,
		Integer maxBranches) {
}

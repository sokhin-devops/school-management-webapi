package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.PlanStatus;

public record PlanResponse(
		UUID id,
		String code,
		String name,
		String description,
		BigDecimal priceMonthly,
		BigDecimal priceYearly,
		String currency,
		Integer maxStudents,
		Integer maxTeachers,
		Integer maxBranches,
		PlanStatus status,
		List<PlanFeatureResponse> features) {
}

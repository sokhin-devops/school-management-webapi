package com.school_management_webapi.dto.response;

public record PlanFeatureResponse(
		String code,
		String name,
		boolean enabled,
		Integer limitValue) {
}

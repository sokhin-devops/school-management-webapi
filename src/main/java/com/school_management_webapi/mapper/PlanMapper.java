package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.response.PlanFeatureResponse;
import com.school_management_webapi.dto.response.PlanResponse;
import com.school_management_webapi.dto.response.PlanSummaryResponse;
import com.school_management_webapi.entity.Plan;
import com.school_management_webapi.entity.PlanFeature;

public final class PlanMapper {

	private PlanMapper() {
	}

	public static PlanResponse toResponse(Plan plan) {
		return new PlanResponse(
				plan.getId(),
				plan.getCode(),
				plan.getName(),
				plan.getDescription(),
				plan.getPriceMonthly(),
				plan.getPriceYearly(),
				plan.getCurrency(),
				plan.getMaxStudents(),
				plan.getMaxTeachers(),
				plan.getMaxBranches(),
				plan.getStatus(),
				plan.getFeatures().stream().map(PlanMapper::toFeatureResponse).toList());
	}

	public static PlanFeatureResponse toFeatureResponse(PlanFeature feature) {
		return new PlanFeatureResponse(
				feature.getFeatureCode(),
				feature.getFeatureName(),
				feature.isEnabled(),
				feature.getLimitValue());
	}

	public static PlanSummaryResponse toSummaryResponse(Plan plan) {
		return new PlanSummaryResponse(
				plan.getId(),
				plan.getCode(),
				plan.getName(),
				plan.getPriceMonthly(),
				plan.getPriceYearly(),
				plan.getCurrency(),
				plan.getMaxStudents(),
				plan.getMaxTeachers(),
				plan.getMaxBranches());
	}
}

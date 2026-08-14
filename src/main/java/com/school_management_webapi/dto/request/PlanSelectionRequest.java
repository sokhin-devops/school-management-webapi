package com.school_management_webapi.dto.request;

import java.util.UUID;

import com.school_management_webapi.entity.BillingCycle;

import jakarta.validation.constraints.NotNull;

public record PlanSelectionRequest(
		@NotNull(message = "planId is required") UUID planId,

		@NotNull(message = "billingCycle is required") BillingCycle billingCycle) {
}

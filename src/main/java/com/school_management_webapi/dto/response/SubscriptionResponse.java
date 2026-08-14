package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.BillingCycle;
import com.school_management_webapi.entity.SubscriptionStatus;

public record SubscriptionResponse(
		UUID id,
		PlanSummaryResponse plan,
		BillingCycle billingCycle,
		SubscriptionStatus status,
		LocalDateTime startAt,
		LocalDateTime endAt,
		LocalDateTime trialEndAt,
		LocalDateTime canceledAt) {
}

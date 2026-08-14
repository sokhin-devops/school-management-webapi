package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.response.SubscriptionResponse;
import com.school_management_webapi.entity.Subscription;

public final class SubscriptionMapper {

	private SubscriptionMapper() {
	}

	public static SubscriptionResponse toResponse(Subscription subscription) {
		return new SubscriptionResponse(
				subscription.getId(),
				PlanMapper.toSummaryResponse(subscription.getPlan()),
				subscription.getBillingCycle(),
				subscription.getStatus(),
				subscription.getStartAt(),
				subscription.getEndAt(),
				subscription.getTrialEndAt(),
				subscription.getCanceledAt());
	}
}

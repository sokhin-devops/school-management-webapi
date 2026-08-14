package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.dto.request.CancelSubscriptionRequest;
import com.school_management_webapi.dto.request.PlanSelectionRequest;
import com.school_management_webapi.dto.response.SubscriptionCancelResponse;
import com.school_management_webapi.dto.response.SubscriptionResponse;

public interface SubscriptionService {

	SubscriptionResponse selectPlan(UUID userId, PlanSelectionRequest request);

	SubscriptionResponse getCurrent(UUID userId);

	List<SubscriptionResponse> getHistory(UUID userId);

	SubscriptionResponse changePlan(UUID userId, PlanSelectionRequest request);

	SubscriptionCancelResponse cancel(UUID userId, CancelSubscriptionRequest request);
}

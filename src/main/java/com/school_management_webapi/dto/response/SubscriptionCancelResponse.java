package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;

import com.school_management_webapi.entity.SubscriptionStatus;

public record SubscriptionCancelResponse(
		SubscriptionStatus status,
		LocalDateTime canceledAt) {
}

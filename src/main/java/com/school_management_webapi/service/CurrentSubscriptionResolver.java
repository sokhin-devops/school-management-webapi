package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.school_management_webapi.entity.Subscription;
import com.school_management_webapi.entity.SubscriptionStatus;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentSubscriptionResolver {

	static final List<SubscriptionStatus> LIVE_STATUSES = List.of(
			SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE);

	private final SubscriptionRepository subscriptionRepository;

	public Subscription resolveActive(UUID tenantId) {
		return subscriptionRepository.findFirstByTenantIdAndStatusInOrderByStartAtDesc(tenantId, LIVE_STATUSES)
				.orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "SUBSCRIPTION_REQUIRED",
						"Tenant has no active subscription"));
	}

	public boolean hasActiveSubscription(UUID tenantId) {
		return subscriptionRepository.existsByTenantIdAndStatusIn(tenantId, LIVE_STATUSES);
	}
}

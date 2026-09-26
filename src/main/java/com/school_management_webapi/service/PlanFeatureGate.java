package com.school_management_webapi.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.entity.PlanFeature;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.PlanFeatureRepository;
import com.school_management_webapi.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

/**
 * What the school's plan includes - 68-subscription.md sells plans by feature,
 * so a feature the plan leaves out is a module the school does not get.
 *
 * A tenant with no live subscription is not gated at all: that is a billing
 * state for the subscription screens to resolve, and locking a school out of
 * its own records over it would be the wrong place to say so.
 */
@Service
@RequiredArgsConstructor
public class PlanFeatureGate {

	private final SubscriptionRepository subscriptionRepository;
	private final PlanFeatureRepository planFeatureRepository;

	/** The live plan's features, enabled or not; empty when there is no live plan. */
	@Transactional(readOnly = true)
	public Optional<List<PlanFeature>> featuresOf(UUID tenantId) {
		return subscriptionRepository
				.findFirstByTenantIdAndStatusInOrderByStartAtDesc(tenantId, CurrentSubscriptionResolver.LIVE_STATUSES)
				.map(subscription -> planFeatureRepository.findByPlanId(subscription.getPlan().getId()));
	}

	/** The codes the plan switches on, or empty when nothing is gated. */
	@Transactional(readOnly = true)
	public Optional<List<String>> enabledCodes(UUID tenantId) {
		return featuresOf(tenantId).map(features -> features.stream()
				.filter(PlanFeature::isEnabled)
				.map(PlanFeature::getFeatureCode)
				.toList());
	}

	@Transactional(readOnly = true)
	public void require(UUID tenantId, String featureCode) {
		Optional<List<PlanFeature>> features = featuresOf(tenantId);
		if (features.isEmpty()) {
			return;
		}
		Optional<PlanFeature> feature = features.get().stream()
				.filter(candidate -> featureCode.equals(candidate.getFeatureCode()))
				.findFirst();
		// A feature the plan does not mention at all is one it was never meant
		// to sell separately, so it is not withheld.
		if (feature.isEmpty() || feature.get().isEnabled()) {
			return;
		}
		throw new ApiException(HttpStatus.FORBIDDEN, "FEATURE_NOT_AVAILABLE",
				feature.get().getFeatureName() + " is not included in your plan. Upgrade under Settings > Subscription.");
	}
}

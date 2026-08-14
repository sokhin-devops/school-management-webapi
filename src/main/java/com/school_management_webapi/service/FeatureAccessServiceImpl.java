package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.entity.PlanFeature;
import com.school_management_webapi.entity.Subscription;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.PlanFeatureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeatureAccessServiceImpl implements FeatureAccessService {

	private final CurrentSubscriptionResolver currentSubscriptionResolver;
	private final PlanFeatureRepository planFeatureRepository;

	@Override
	public boolean hasFeature(UUID tenantId, String featureCode) {
		Subscription subscription = currentSubscriptionResolver.resolveActive(tenantId);
		return planFeatureRepository.findByPlanIdAndFeatureCode(subscription.getPlan().getId(), featureCode)
				.map(PlanFeature::isEnabled)
				.orElse(false);
	}

	@Override
	public void requireFeature(UUID tenantId, String featureCode) {
		if (!hasFeature(tenantId, featureCode)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FEATURE_NOT_AVAILABLE",
					"Feature '" + featureCode + "' is not available on the current plan");
		}
	}
}

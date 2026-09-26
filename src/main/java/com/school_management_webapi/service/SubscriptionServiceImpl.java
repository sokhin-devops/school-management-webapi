package com.school_management_webapi.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.CancelSubscriptionRequest;
import com.school_management_webapi.dto.request.PlanSelectionRequest;
import com.school_management_webapi.dto.response.SubscriptionCancelResponse;
import com.school_management_webapi.dto.response.SubscriptionResponse;
import com.school_management_webapi.entity.AuditAction;
import com.school_management_webapi.entity.Plan;
import com.school_management_webapi.entity.PlanStatus;
import com.school_management_webapi.entity.Subscription;
import com.school_management_webapi.entity.SubscriptionStatus;
import com.school_management_webapi.entity.Tenant;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.mapper.SubscriptionMapper;
import com.school_management_webapi.repository.PlanRepository;
import com.school_management_webapi.repository.SubscriptionRepository;
import com.school_management_webapi.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

	private static final List<SubscriptionStatus> LIVE_STATUSES = CurrentSubscriptionResolver.LIVE_STATUSES;

	private final SubscriptionRepository subscriptionRepository;
	private final PlanRepository planRepository;
	private final TenantRepository tenantRepository;
	private final TenantAuthorizationService tenantAuthorizationService;
	private final SubscriptionBillingService billingService;
	private final AuditService auditService;

	@Override
	public SubscriptionResponse selectPlan(UUID userId, PlanSelectionRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);

		if (subscriptionRepository.existsByTenantIdAndStatusIn(tenantId, LIVE_STATUSES)) {
			throw new ApiException(HttpStatus.CONFLICT, "SUBSCRIPTION_ALREADY_EXISTS",
					"Tenant already has an active subscription. Use change plan instead.");
		}

		Plan plan = resolveActivePlan(request.planId());
		Tenant tenant = tenantRepository.getReferenceById(tenantId);

		Subscription subscription = Subscription.builder()
				.tenant(tenant)
				.plan(plan)
				.status(SubscriptionStatus.ACTIVE)
				.billingCycle(request.billingCycle())
				.startAt(LocalDateTime.now())
				.build();

		Subscription saved = subscriptionRepository.save(subscription);
		// The first period starts now and is billed; without an end date there
		// was nothing to show as "Renews on".
		billingService.startPeriod(saved);
		return SubscriptionMapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public SubscriptionResponse getCurrent(UUID userId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return SubscriptionMapper.toResponse(findCurrentOrThrow(tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<SubscriptionResponse> getHistory(UUID userId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return subscriptionRepository.findAllByTenantIdOrderByStartAtDesc(tenantId).stream()
				.map(SubscriptionMapper::toResponse)
				.toList();
	}

	@Override
	public SubscriptionResponse changePlan(UUID userId, PlanSelectionRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		Subscription current = findCurrentOrThrow(tenantId);

		if (!LIVE_STATUSES.contains(current.getStatus())) {
			throw new ApiException(HttpStatus.CONFLICT, "SUBSCRIPTION_REQUIRED",
					"Tenant has no active subscription to change.");
		}

		Plan newPlan = resolveActivePlan(request.planId());
		boolean upgrade = newPlan.getSortOrder() > current.getPlan().getSortOrder();
		log.info("Tenant {} changing plan {} -> {} ({})", tenantId, current.getPlan().getCode(), newPlan.getCode(),
				upgrade ? "upgrade" : "downgrade");

		String previous = current.getPlan().getName();
		current.setPlan(newPlan);
		current.setBillingCycle(request.billingCycle());

		Subscription saved = subscriptionRepository.save(current);
		// A change starts a new period on the new price, so the invoice says what
		// the school is now paying for.
		billingService.startPeriod(saved);
		auditService.record(userId, AuditAction.PLAN_CHANGED, previous + " -> " + newPlan.getName());
		return SubscriptionMapper.toResponse(saved);
	}

	@Override
	public SubscriptionCancelResponse cancel(UUID userId, CancelSubscriptionRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		Subscription current = findCurrentOrThrow(tenantId);

		if (!LIVE_STATUSES.contains(current.getStatus())) {
			throw new ApiException(HttpStatus.CONFLICT, "SUBSCRIPTION_REQUIRED",
					"Tenant has no active subscription to cancel.");
		}

		current.setStatus(SubscriptionStatus.CANCELED);
		current.setCanceledAt(LocalDateTime.now());
		subscriptionRepository.save(current);

		log.info("Tenant {} canceled subscription {}. Reason: {}", tenantId, current.getId(), request.reason());
		auditService.record(userId, AuditAction.PLAN_CANCELED, request.reason());

		return new SubscriptionCancelResponse(current.getStatus(), current.getCanceledAt());
	}

	private Subscription findCurrentOrThrow(UUID tenantId) {
		return subscriptionRepository.findFirstByTenantIdOrderByStartAtDesc(tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SUBSCRIPTION_NOT_FOUND",
						"No subscription found for this tenant."));
	}

	private Plan resolveActivePlan(UUID planId) {
		Plan plan = planRepository.findById(planId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PLAN_NOT_FOUND", "Plan not found."));
		if (plan.getStatus() != PlanStatus.ACTIVE) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "PLAN_INACTIVE", "Plan is not active.");
		}
		return plan;
	}
}

package com.school_management_webapi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.response.SettingsResponse;
import com.school_management_webapi.entity.BillingCycle;
import com.school_management_webapi.entity.Plan;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.entity.Subscription;
import com.school_management_webapi.entity.SubscriptionInvoice;
import com.school_management_webapi.entity.SubscriptionStatus;
import com.school_management_webapi.entity.TenantSettings;
import com.school_management_webapi.entity.TenantUserRole;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.repository.BranchRepository;
import com.school_management_webapi.repository.SchoolRepository;
import com.school_management_webapi.repository.StudentRepository;
import com.school_management_webapi.repository.SubscriptionInvoiceRepository;
import com.school_management_webapi.repository.SubscriptionRepository;
import com.school_management_webapi.repository.TeacherRepository;
import com.school_management_webapi.repository.TenantUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 68-subscription.md's money side: billing periods, invoices, and what the
 * plan's limits are being used for.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionBillingService {

	private final SubscriptionInvoiceRepository invoiceRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final TenantAuthorizationService tenantAuthorizationService;
	private final TenantSettingsService tenantSettingsService;
	private final CurrentSubscriptionResolver currentSubscriptionResolver;
	private final SchoolRepository schoolRepository;
	private final BranchRepository branchRepository;
	private final StudentRepository studentRepository;
	private final TeacherRepository teacherRepository;
	private final TenantUserRepository tenantUserRepository;

	/** Starts a billing period now and bills it. Called when a plan is chosen or changed. */
	@Transactional
	public void startPeriod(Subscription subscription) {
		LocalDateTime now = LocalDateTime.now();
		subscription.setStartAt(now);
		subscription.setEndAt(periodEnd(now, subscription.getBillingCycle()));
		subscriptionRepository.save(subscription);
		issueInvoice(subscription);
	}

	/**
	 * Rolls every active subscription whose period has ended into the next one,
	 * billing each. Run daily; a period missed while the server was down is
	 * caught up one period per run, so no school is ever billed twice at once.
	 */
	@Transactional
	public int renewDue() {
		LocalDateTime now = LocalDateTime.now();
		int renewed = 0;
		for (Subscription subscription : subscriptionRepository.findAll()) {
			if (subscription.getStatus() != SubscriptionStatus.ACTIVE || subscription.getEndAt() == null
					|| subscription.getEndAt().isAfter(now)) {
				continue;
			}
			LocalDateTime start = subscription.getEndAt();
			subscription.setStartAt(start);
			subscription.setEndAt(periodEnd(start, subscription.getBillingCycle()));
			subscriptionRepository.save(subscription);
			issueInvoice(subscription);
			renewed++;
		}
		if (renewed > 0) {
			log.info("Renewed {} subscription period(s)", renewed);
		}
		return renewed;
	}

	@Transactional(readOnly = true)
	public SettingsResponse.Usage usage(UUID userId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		List<UUID> schoolIds = schoolRepository.findIdsByTenantId(tenantId);
		List<UUID> branchIds = branchRepository.findIdsByTenantId(tenantId);

		long students = schoolIds.isEmpty() ? 0 : studentRepository.countBySchoolIdIn(schoolIds);
		long teachers = branchIds.isEmpty() ? 0 : teacherRepository.countByBranchIdIn(branchIds);
		long branches = branchRepository.countByTenantId(tenantId);

		Plan plan = currentSubscriptionResolver.hasActiveSubscription(tenantId)
				? currentSubscriptionResolver.resolveActive(tenantId).getPlan()
				: null;
		return new SettingsResponse.Usage(plan == null ? null : plan.getName(), List.of(
				new SettingsResponse.UsageRow("students", "Students", students, plan == null ? null : plan.getMaxStudents()),
				new SettingsResponse.UsageRow("teachers", "Teachers", teachers, plan == null ? null : plan.getMaxTeachers()),
				new SettingsResponse.UsageRow("branches", "Branches", branches, plan == null ? null : plan.getMaxBranches())));
	}

	@Transactional(readOnly = true)
	public List<SettingsResponse.Invoice> invoices(UUID userId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return invoiceRepository.findByTenantIdOrderByIssuedAtDesc(tenantId).stream()
				.map(SubscriptionBillingService::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public SettingsResponse.Invoice invoice(UUID userId, UUID invoiceId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
				.map(SubscriptionBillingService::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
	}

	// --- internals --------------------------------------------------------------

	private void issueInvoice(Subscription subscription) {
		UUID tenantId = subscription.getTenant().getId();
		Plan plan = subscription.getPlan();
		BigDecimal amount = subscription.getBillingCycle() == BillingCycle.YEARLY ? plan.getPriceYearly()
				: plan.getPriceMonthly();

		TenantSettings settings = tenantSettingsService.forTenant(tenantId);
		List<School> schools = schoolRepository.findByTenantIdOrderByCreatedAtAsc(tenantId);
		String billToName = schools.isEmpty() ? subscription.getTenant().getName() : schools.get(0).getName();
		String billToEmail = settings.getBillingEmail() != null ? settings.getBillingEmail() : ownerEmail(tenantId);

		long sequence = invoiceRepository.countByTenantId(tenantId) + 1;
		invoiceRepository.save(SubscriptionInvoice.builder()
				.tenantId(tenantId)
				.subscriptionId(subscription.getId())
				.number(String.format("INV-%d-%04d", subscription.getStartAt().getYear(), sequence))
				.planName(plan.getName())
				.billingCycle(subscription.getBillingCycle())
				.amount(amount == null ? BigDecimal.ZERO : amount)
				.currency(plan.getCurrency() == null ? "USD" : plan.getCurrency())
				.periodStart(subscription.getStartAt())
				.periodEnd(subscription.getEndAt())
				.billToName(billToName)
				.billToEmail(billToEmail)
				.billToAddress(settings.getBillingAddress())
				.build());
	}

	private String ownerEmail(UUID tenantId) {
		return tenantUserRepository.findByTenantIdOrderByCreatedAtAsc(tenantId).stream()
				.filter(member -> member.getRole() == TenantUserRole.OWNER)
				.map(member -> member.getUser().getEmail())
				.findFirst()
				.orElse(null);
	}

	private static LocalDateTime periodEnd(LocalDateTime start, BillingCycle cycle) {
		return cycle == BillingCycle.YEARLY ? start.plusYears(1) : start.plusMonths(1);
	}

	private static SettingsResponse.Invoice toResponse(SubscriptionInvoice invoice) {
		return new SettingsResponse.Invoice(invoice.getId(), invoice.getNumber(), invoice.getPlanName(),
				invoice.getBillingCycle(), invoice.getAmount(), invoice.getCurrency(), invoice.getPeriodStart(),
				invoice.getPeriodEnd(), invoice.getIssuedAt(), invoice.getBillToName(), invoice.getBillToEmail(),
				invoice.getBillToAddress());
	}
}

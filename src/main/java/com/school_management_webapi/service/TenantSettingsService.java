package com.school_management_webapi.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.SettingsRequest;
import com.school_management_webapi.dto.response.SettingsResponse;
import com.school_management_webapi.entity.AcademicPreferences;
import com.school_management_webapi.entity.AuditAction;
import com.school_management_webapi.entity.SecurityPolicy;
import com.school_management_webapi.entity.TenantSettings;
import com.school_management_webapi.repository.TenantSettingsRepository;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.TenantUserRepository;
import com.school_management_webapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Reads and writes a tenant's settings row, creating it with defaults the first
 * time it is needed.
 */
@Service
@RequiredArgsConstructor
public class TenantSettingsService {

	private final TenantSettingsRepository tenantSettingsRepository;
	private final TenantUserRepository tenantUserRepository;
	private final TenantAuthorizationService tenantAuthorizationService;
	private final AuditService auditService;
	private final NotificationService notificationService;
	private final UserRepository userRepository;

	// --- read by the rest of the system ---------------------------------------

	/** The policy that governs this user's password, or the platform default outside any tenant. */
	@Transactional(readOnly = true)
	public SecurityPolicy policyForUser(UUID userId) {
		return tenantOf(userId)
				.flatMap(tenantSettingsRepository::findByTenantId)
				.map(TenantSettings::getSecurity)
				.orElseGet(SecurityPolicy::defaults);
	}

	@Transactional(readOnly = true)
	public boolean isMaintenance(UUID tenantId) {
		return tenantSettingsRepository.findByTenantId(tenantId).map(TenantSettings::isMaintenanceMode).orElse(false);
	}

	@Transactional(readOnly = true)
	public AcademicPreferences academicFor(UUID tenantId) {
		return tenantSettingsRepository.findByTenantId(tenantId)
				.map(TenantSettings::getAcademic)
				.orElseGet(AcademicPreferences::defaults);
	}

	// --- Settings screens -----------------------------------------------------

	@Transactional
	public SettingsResponse.Security getSecurity(UUID userId) {
		return toSecurity(require(userId).getSecurity());
	}

	@Transactional
	public SettingsResponse.Security updateSecurity(UUID userId, SettingsRequest.Security request) {
		TenantSettings settings = require(userId);
		SecurityPolicy policy = settings.getSecurity();
		policy.setPasswordMinLength(Math.max(SecurityPolicy.MIN_LENGTH_FLOOR, request.passwordMinLength()));
		policy.setPasswordRequireUppercase(request.passwordRequireUppercase());
		policy.setPasswordRequireNumber(request.passwordRequireNumber());
		policy.setPasswordRequireSymbol(request.passwordRequireSymbol());
		policy.setPasswordExpiryDays(request.passwordExpiryDays());
		policy.setSignOutOnPasswordChange(request.signOutOnPasswordChange());
		policy.setSessionTimeoutMinutes(request.sessionTimeoutMinutes());
		// Requiring it of everyone, while the person switching it on has not set
		// it up, would stop them at the next request - on this very page.
		if (request.twoFactorRequired() && !policy.isTwoFactorRequired()
				&& userRepository.findById(userId).map(user -> !user.isTwoFactorEnabled()).orElse(true)) {
			throw new ApiException(HttpStatus.CONFLICT, "TWO_FACTOR_SETUP_FIRST",
					"Turn on two-factor sign-in for your own account first (your name, top right > Two-factor sign-in).");
		}
		policy.setTwoFactorRequired(request.twoFactorRequired());
		tenantSettingsRepository.save(settings);
		auditService.record(userId, AuditAction.SECURITY_SETTINGS_CHANGED, describe(policy));
		return toSecurity(policy);
	}

	@Transactional
	public SettingsResponse.Academic getAcademic(UUID userId) {
		return toAcademic(require(userId).getAcademic());
	}

	@Transactional
	public SettingsResponse.Academic updateAcademic(UUID userId, SettingsRequest.Academic request) {
		TenantSettings settings = require(userId);
		AcademicPreferences academic = settings.getAcademic();
		academic.setUsePrograms(request.usePrograms());
		academic.setUseLevels(request.useLevels());
		academic.setUseSections(request.useSections());
		academic.setUseSubjects(request.useSubjects());
		academic.setUseTerms(request.useTerms());
		academic.setUseRooms(request.useRooms());
		academic.setLevelLabel(request.levelLabel().trim());
		academic.setClassLabel(request.classLabel().trim());
		academic.setSubjectLabel(request.subjectLabel().trim());
		academic.setStudentLabel(request.studentLabel().trim());
		academic.setTeacherLabel(request.teacherLabel().trim());
		academic.setAutoRollover(request.autoRollover());
		academic.setTermStructure(request.termStructure());
		academic.setGradingScale(request.gradingScale());
		academic.setPassMark(request.passMark());
		tenantSettingsRepository.save(settings);
		auditService.record(userId, AuditAction.ACADEMIC_SETTINGS_CHANGED, null);
		return toAcademic(academic);
	}

	@Transactional
	public SettingsResponse.SystemSettings getSystem(UUID userId) {
		return new SettingsResponse.SystemSettings(require(userId).isMaintenanceMode());
	}

	@Transactional
	public SettingsResponse.SystemSettings updateSystem(UUID userId, SettingsRequest.SystemSettings request) {
		TenantSettings settings = require(userId);
		boolean changed = settings.isMaintenanceMode() != request.maintenanceMode();
		settings.setMaintenanceMode(request.maintenanceMode());
		tenantSettingsRepository.save(settings);
		if (changed) {
			auditService.record(userId, AuditAction.MAINTENANCE_MODE_CHANGED,
					request.maintenanceMode() ? "Maintenance mode turned on" : "Maintenance mode turned off");
			notificationService.notifyTenant(settings.getTenantId(), NotificationEvent.MAINTENANCE,
					request.maintenanceMode()
							? "The school is offline for maintenance. Only owners can sign in until it is back."
							: "Maintenance is over; everyone can sign in again.",
					null);
		}
		return new SettingsResponse.SystemSettings(settings.isMaintenanceMode());
	}

	@Transactional
	public SettingsResponse.Billing getBilling(UUID userId) {
		TenantSettings settings = require(userId);
		return new SettingsResponse.Billing(settings.getBillingEmail(), settings.getBillingAddress());
	}

	@Transactional
	public SettingsResponse.Billing updateBilling(UUID userId, SettingsRequest.Billing request) {
		TenantSettings settings = require(userId);
		settings.setBillingEmail(request.billingEmail().trim());
		settings.setBillingAddress(request.billingAddress() == null ? null : request.billingAddress().trim());
		tenantSettingsRepository.save(settings);
		return new SettingsResponse.Billing(settings.getBillingEmail(), settings.getBillingAddress());
	}

	/** The row for a tenant, created on first use. */
	@Transactional
	public TenantSettings forTenant(UUID tenantId) {
		return tenantSettingsRepository.findByTenantId(tenantId).orElseGet(() -> create(tenantId));
	}

	// --- internals ------------------------------------------------------------

	private TenantSettings require(UUID userId) {
		return forTenant(tenantAuthorizationService.requireTenantId(userId));
	}

	/**
	 * The unique tenant id means two first requests at once cannot both insert:
	 * one wins and the other fails its request, and the row exists for its retry.
	 * Rare enough - it needs two people opening Settings in the same instant on a
	 * tenant that has never had it opened - not to be worth more machinery.
	 */
	private TenantSettings create(UUID tenantId) {
		return tenantSettingsRepository.saveAndFlush(TenantSettings.builder().tenantId(tenantId).build());
	}

	private Optional<UUID> tenantOf(UUID userId) {
		return tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
				.map(membership -> membership.getTenant().getId());
	}

	private static SettingsResponse.Security toSecurity(SecurityPolicy policy) {
		return new SettingsResponse.Security(policy.getPasswordMinLength(), policy.isPasswordRequireUppercase(),
				policy.isPasswordRequireNumber(), policy.isPasswordRequireSymbol(), policy.getPasswordExpiryDays(),
				policy.isSignOutOnPasswordChange(), policy.getSessionTimeoutMinutes(), policy.isTwoFactorRequired(),
				true);
	}

	private static SettingsResponse.Academic toAcademic(AcademicPreferences academic) {
		return new SettingsResponse.Academic(academic.isUsePrograms(), academic.isUseLevels(),
				academic.isUseSections(), academic.isUseSubjects(), academic.isUseTerms(), academic.isUseRooms(),
				academic.getLevelLabel(), academic.getClassLabel(), academic.getSubjectLabel(),
				academic.getStudentLabel(), academic.getTeacherLabel(), academic.isAutoRollover(),
				academic.getTermStructure(), academic.getGradingScale(), academic.getPassMark());
	}

	private static String describe(SecurityPolicy policy) {
		return "Passwords " + policy.getPasswordMinLength() + "+ characters"
				+ (policy.isPasswordRequireUppercase() ? ", uppercase" : "")
				+ (policy.isPasswordRequireNumber() ? ", number" : "")
				+ (policy.isPasswordRequireSymbol() ? ", symbol" : "")
				+ "; session timeout " + policy.getSessionTimeoutMinutes() + " min";
	}
}

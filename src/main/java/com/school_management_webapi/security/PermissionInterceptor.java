package com.school_management_webapi.security;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.school_management_webapi.entity.PermissionAction;
import com.school_management_webapi.entity.TenantUserRole;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.TenantUserRepository;
import com.school_management_webapi.service.PermissionService;
import com.school_management_webapi.service.PlanFeatureGate;
import com.school_management_webapi.service.TenantSettingsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Turns the role grid into an answer of yes or no on every request.
 *
 * Authentication has already run by the time this is reached, so what is left
 * is whether this signed-in user is allowed this module and this action.
 */
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

	private final PermissionService permissionService;
	private final TenantUserRepository tenantUserRepository;
	private final TenantSettingsService tenantSettingsService;
	private final PlanFeatureGate planFeatureGate;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		// Anything that is not a controller method - the error dispatch, static
		// resources, the CORS preflight - carries no module to check.
		if (!(handler instanceof HandlerMethod) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}

		Authentication signedIn = SecurityContextHolder.getContext().getAuthentication();
		if (signedIn != null && signedIn.getPrincipal() instanceof UserPrincipal caller
				&& !request.getRequestURI().startsWith("/api/v1/auth/")) {
			requireNotInMaintenance(caller);
			requireTwoFactorWhereRequired(caller);
		}

		Optional<PermissionAction> action = ModulePermissions.actionFor(request.getMethod());
		if (action.isEmpty()) {
			return true;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			// Nobody signed in: Spring Security has already refused anything that
			// needs a session, so there is no plan or grid to check a caller against.
			return true;
		}

		// The plan first, and for everyone, owners included: it is what the school
		// bought, not what a role allows.
		Optional<String> feature = ModulePermissions.featureFor(request.getRequestURI(), action.get());
		if (feature.isPresent()) {
			tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(principal.getUser().getId())
					.ifPresent(membership -> planFeatureGate.require(membership.getTenant().getId(), feature.get()));
		}

		Optional<String> module = ModulePermissions.moduleFor(request.getRequestURI(), action.get());
		if (module.isEmpty()) {
			return true;
		}

		if (!permissionService.isAllowed(principal.getUser().getId(), module.get(), action.get())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
					"Your role does not allow you to " + action.get().name().toLowerCase() + " " + module.get() + ".");
		}

		return true;
	}

	/**
	 * 67-security.md: a school that requires two-factor lets its people sign in
	 * with the password so they can set it up, and nothing else until they
	 * have. /auth - where setup lives - stays open.
	 */
	private void requireTwoFactorWhereRequired(UserPrincipal caller) {
		if (!caller.getUser().isTwoFactorEnabled()
				&& tenantSettingsService.policyForUser(caller.getUser().getId()).isTwoFactorRequired()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "TWO_FACTOR_SETUP_REQUIRED",
					"Your school requires two-factor sign-in. Set it up to continue.");
		}
	}

	/**
	 * 69-system-settings.md: while a school is in maintenance only its owners
	 * work in it. Sign-in and refresh refuse everyone else too; this covers a
	 * token they already held when it was switched on. /auth stays open so they
	 * can still sign out.
	 */
	private void requireNotInMaintenance(UserPrincipal caller) {
		tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(caller.getUser().getId()).ifPresent(membership -> {
			if (membership.getRole() != TenantUserRole.OWNER
					&& tenantSettingsService.isMaintenance(membership.getTenant().getId())) {
				throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "MAINTENANCE_MODE",
						"Your school is offline for maintenance. Try again later.");
			}
		});
	}
}

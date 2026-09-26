package com.school_management_webapi.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.entity.SecurityPolicy;
import com.school_management_webapi.entity.AuditAction;
import com.school_management_webapi.dto.request.ForgotPasswordRequest;
import com.school_management_webapi.dto.request.LoginRequest;
import com.school_management_webapi.dto.request.TwoFactorRequest;
import com.school_management_webapi.dto.request.RegisterRequest;
import com.school_management_webapi.dto.request.ResetPasswordRequest;
import com.school_management_webapi.dto.response.AuthResponse;
import com.school_management_webapi.dto.response.ForgotPasswordResponse;
import com.school_management_webapi.dto.response.UserResponse;
import com.school_management_webapi.entity.PasswordResetToken;
import com.school_management_webapi.entity.RefreshToken;
import com.school_management_webapi.entity.Tenant;
import com.school_management_webapi.entity.TenantStatus;
import com.school_management_webapi.entity.TenantUser;
import com.school_management_webapi.entity.TenantUserRole;
import com.school_management_webapi.entity.User;
import com.school_management_webapi.entity.UserStatus;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.InvalidCredentialsException;
import com.school_management_webapi.exception.InvalidTokenException;
import com.school_management_webapi.exception.PasswordMismatchException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.UserMapper;
import com.school_management_webapi.repository.PasswordResetTokenRepository;
import com.school_management_webapi.repository.RefreshTokenRepository;
import com.school_management_webapi.repository.TenantRepository;
import com.school_management_webapi.repository.TenantUserRepository;
import com.school_management_webapi.repository.UserRepository;
import com.school_management_webapi.security.JwtService;
import com.school_management_webapi.security.TokenHasher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

	private static final long PASSWORD_RESET_TOKEN_TTL_MINUTES = 30;

	@Value("${app.password-reset.expose-token:true}")
	private boolean exposeResetTokenInResponse;

	@Value("${app.frontend.reset-password-url:http://localhost:4200/reset-password}")
	private String resetPasswordUrl;

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final TenantRepository tenantRepository;
	private final TenantUserRepository tenantUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final EmailService emailService;
	private final TenantSettingsService tenantSettingsService;
	private final PasswordPolicyService passwordPolicyService;
	private final AuditService auditService;
	private final NotificationService notificationService;
	private final TwoFactorService twoFactorService;

	@Override
	public AuthResponse register(RegisterRequest request) {
		if (!request.password().equals(request.confirmPassword())) {
			throw new PasswordMismatchException("password and confirmPassword do not match");
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new DuplicateResourceException("An account with email '" + request.email() + "' already exists");
		}

		User user = User.builder()
				.name(request.name())
				.email(request.email())
				.passwordHash(passwordEncoder.encode(request.password()))
				.passwordChangedAt(LocalDateTime.now())
				.status(UserStatus.ACTIVE)
				.build();
		User saved = userRepository.save(user);

		Tenant tenant = tenantRepository.save(Tenant.builder()
				.name(request.name() + "'s Organization")
				.status(TenantStatus.ACTIVE)
				.build());

		tenantUserRepository.save(TenantUser.builder()
				.tenant(tenant)
				.user(saved)
				.role(TenantUserRole.OWNER)
				.build());

		return openSession(saved);
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			// Recorded against the account that was tried, so an owner can see a
			// run of failures on someone's account in the audit log.
			auditService.recordFor(user, AuditAction.SIGN_IN_FAILED, "Wrong password");
			throw new InvalidCredentialsException("Invalid email or password");
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new InvalidCredentialsException("Account is not active");
		}

		requireNotInMaintenance(user);

		SecurityPolicy policy = tenantSettingsService.policyForUser(user.getId());
		if (passwordPolicyService.isExpired(policy, user)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "PASSWORD_EXPIRED",
					"Your password has expired. Use \"Forgot password\" to set a new one.");
		}

		// 67-security.md: with two-factor on, the password only earns the chance
		// to enter a code. Nothing below - the session, the "last signed in",
		// the new-device notice - happens until the code is right.
		if (user.isTwoFactorEnabled()) {
			return AuthResponse.twoFactorChallenge(jwtService.generateTwoFactorToken(user));
		}
		return completeSignIn(user);
	}

	@Override
	public AuthResponse loginWithTwoFactor(TwoFactorRequest.SignIn request) {
		String challenge = request.twoFactorToken();
		if (!jwtService.isValid(challenge) || !jwtService.isTwoFactorToken(challenge)) {
			throw new InvalidTokenException("That sign-in has expired. Enter your password again.");
		}
		User user = userRepository.findById(jwtService.extractUserId(challenge))
				.orElseThrow(() -> new InvalidTokenException("That sign-in has expired. Enter your password again."));
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new InvalidCredentialsException("Account is not active");
		}
		requireNotInMaintenance(user);

		// Reset by an admin between the two steps: the password was right, and
		// there is no longer a second factor to ask for.
		if (user.isTwoFactorEnabled()
				&& !twoFactorService.verifySignIn(user, jwtService.extractTokenId(challenge), request.code())) {
			auditService.recordFor(user, AuditAction.SIGN_IN_FAILED, "Wrong two-factor code");
			throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TWO_FACTOR_CODE",
					"That code is not right. Use the newest code in your app, or one of your recovery codes.");
		}
		return completeSignIn(user);
	}

	/** Everything a sign-in is, once every factor it needs has been given. */
	private AuthResponse completeSignIn(User user) {
		String userAgent = RequestInfo.userAgent();
		boolean knownDevice = userAgent == null
				|| !refreshTokenRepository.existsByUserId(user.getId())
				|| refreshTokenRepository.existsByUserIdAndUserAgent(user.getId(), userAgent);

		user.setLastLoginAt(LocalDateTime.now());
		userRepository.save(user);

		AuthResponse response = openSession(user);

		String device = RequestInfo.describeDevice(userAgent);
		auditService.record(user.getId(), AuditAction.SIGNED_IN, device);
		if (!knownDevice) {
			notificationService.notifyUser(user.getId(), NotificationEvent.NEW_DEVICE_SIGN_IN,
					"Signed in from " + device + (RequestInfo.ipAddress() != null ? " (" + RequestInfo.ipAddress() + ")" : "")
							+ ". If this was not you, revoke it under Settings > Security.",
					"/settings/security");
		}
		return response;
	}

	@Override
	public void logout(String refreshToken) {
		String tokenHash = TokenHasher.sha256Hex(refreshToken);
		refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
			// The whole sign-in ends, not only this token.
			refreshTokenRepository.revokeSession(token.getUser().getId(), SessionService.sessionOf(token),
					LocalDateTime.now());
			auditService.record(token.getUser().getId(), AuditAction.SIGNED_OUT,
					RequestInfo.describeDevice(token.getUserAgent()));
		});
	}

	@Override
	public AuthResponse refreshToken(String refreshToken) {
		if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
			throw new InvalidTokenException("Invalid or expired refresh token");
		}

		String tokenHash = TokenHasher.sha256Hex(refreshToken);
		RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

		if (stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new InvalidTokenException("Invalid or expired refresh token");
		}

		User user = stored.getUser();
		UUID sessionId = SessionService.sessionOf(stored);
		LocalDateTime now = LocalDateTime.now();

		// 67-security.md's session timeout: a session left unused for longer than
		// the school allows has to sign in again. Measured from the last request
		// the session made, not from the last refresh.
		SecurityPolicy policy = tenantSettingsService.policyForUser(user.getId());
		LocalDateTime lastSeen = stored.getLastSeenAt() != null ? stored.getLastSeenAt() : stored.getCreatedAt();
		if (lastSeen != null && lastSeen.plusMinutes(policy.getSessionTimeoutMinutes()).isBefore(now)) {
			refreshTokenRepository.revokeSession(user.getId(), sessionId, now);
			throw new InvalidTokenException("Your session timed out. Sign in again.");
		}

		requireNotInMaintenance(user);

		stored.setRevokedAt(now);
		refreshTokenRepository.save(stored);

		LocalDateTime startedAt = stored.getSessionStartedAt() != null ? stored.getSessionStartedAt()
				: stored.getCreatedAt();
		return issueAuthResponse(user, sessionId, startedAt);
	}

	@Override
	public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
		String resetUrl = userRepository.findByEmail(request.email()).map(user -> {
			String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
			PasswordResetToken resetToken = PasswordResetToken.builder()
					.user(user)
					.tokenHash(TokenHasher.sha256Hex(rawToken))
					.expiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_TTL_MINUTES))
					.build();
			passwordResetTokenRepository.save(resetToken);
			emailService.sendPasswordResetEmail(user.getEmail(), rawToken);

			return exposeResetTokenInResponse ? resetPasswordUrl + "?token=" + rawToken : null;
		}).orElse(null);

		return new ForgotPasswordResponse(resetUrl);
	}

	@Override
	public void resetPassword(ResetPasswordRequest request) {
		if (!request.password().equals(request.confirmPassword())) {
			throw new PasswordMismatchException("password and confirmPassword do not match");
		}

		String tokenHash = TokenHasher.sha256Hex(request.token());
		PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

		if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new InvalidTokenException("Invalid or expired reset token");
		}

		User user = resetToken.getUser();
		SecurityPolicy policy = tenantSettingsService.policyForUser(user.getId());
		passwordPolicyService.requireAcceptable(policy, request.password());

		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setPasswordChangedAt(LocalDateTime.now());
		userRepository.save(user);

		resetToken.setUsedAt(LocalDateTime.now());
		passwordResetTokenRepository.save(resetToken);

		if (policy.isSignOutOnPasswordChange()) {
			refreshTokenRepository.revokeAllForUser(user.getId(), LocalDateTime.now());
		}
		auditService.recordFor(user, AuditAction.PASSWORD_RESET,
				policy.isSignOutOnPasswordChange() ? "Signed out everywhere" : null);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getCurrentUser(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		return UserMapper.toResponse(user, resolveTenantId(user.getId()));
	}

	/** A new sign-in: a new session. */
	private AuthResponse openSession(User user) {
		return issueAuthResponse(user, UUID.randomUUID(), LocalDateTime.now());
	}

	private AuthResponse issueAuthResponse(User user, UUID sessionId, LocalDateTime sessionStartedAt) {
		String refreshToken = jwtService.generateRefreshToken(user);
		LocalDateTime now = LocalDateTime.now();

		RefreshToken tokenEntity = RefreshToken.builder()
				.user(user)
				.tokenHash(TokenHasher.sha256Hex(refreshToken))
				.expiresAt(now.plusSeconds(jwtService.getRefreshTokenExpirationMs() / 1000))
				.sessionId(sessionId)
				.sessionStartedAt(sessionStartedAt)
				.userAgent(RequestInfo.userAgent())
				.ipAddress(RequestInfo.ipAddress())
				.lastSeenAt(now)
				.build();
		refreshTokenRepository.save(tokenEntity);

		String accessToken = jwtService.generateAccessToken(user, sessionId);

		return new AuthResponse(
				accessToken,
				refreshToken,
				"Bearer",
				jwtService.getAccessTokenExpirationMs() / 1000,
				UserMapper.toResponse(user, resolveTenantId(user.getId())),
				null);
	}

	/**
	 * 69-system-settings.md: while a school is in maintenance, only its owners
	 * get in. Checked at sign-in and at every refresh; the permission
	 * interceptor turns away requests made with a token already held.
	 */
	private void requireNotInMaintenance(User user) {
		tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(user.getId()).ifPresent(membership -> {
			if (membership.getRole() != TenantUserRole.OWNER
					&& tenantSettingsService.isMaintenance(membership.getTenant().getId())) {
				throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "MAINTENANCE_MODE",
						"Your school is offline for maintenance. Try again later.");
			}
		});
	}

	private UUID resolveTenantId(UUID userId) {
		return tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
				.map(tenantUser -> tenantUser.getTenant().getId())
				.orElse(null);
	}
}

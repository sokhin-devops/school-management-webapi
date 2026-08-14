package com.school_management_webapi.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.ForgotPasswordRequest;
import com.school_management_webapi.dto.request.LoginRequest;
import com.school_management_webapi.dto.request.RegisterRequest;
import com.school_management_webapi.dto.request.ResetPasswordRequest;
import com.school_management_webapi.dto.response.AuthResponse;
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

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final TenantRepository tenantRepository;
	private final TenantUserRepository tenantUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final EmailService emailService;

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

		return issueAuthResponse(saved);
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException("Invalid email or password");
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new InvalidCredentialsException("Account is not active");
		}

		user.setLastLoginAt(LocalDateTime.now());
		userRepository.save(user);

		return issueAuthResponse(user);
	}

	@Override
	public void logout(String refreshToken) {
		String tokenHash = TokenHasher.sha256Hex(refreshToken);
		refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
			token.setRevokedAt(LocalDateTime.now());
			refreshTokenRepository.save(token);
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

		stored.setRevokedAt(LocalDateTime.now());
		refreshTokenRepository.save(stored);

		return issueAuthResponse(stored.getUser());
	}

	@Override
	public void forgotPassword(ForgotPasswordRequest request) {
		userRepository.findByEmail(request.email()).ifPresent(user -> {
			String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
			PasswordResetToken resetToken = PasswordResetToken.builder()
					.user(user)
					.tokenHash(TokenHasher.sha256Hex(rawToken))
					.expiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_TTL_MINUTES))
					.build();
			passwordResetTokenRepository.save(resetToken);
			emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
		});
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
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		userRepository.save(user);

		resetToken.setUsedAt(LocalDateTime.now());
		passwordResetTokenRepository.save(resetToken);

		refreshTokenRepository.revokeAllForUser(user.getId(), LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getCurrentUser(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		return UserMapper.toResponse(user, resolveTenantId(user.getId()));
	}

	private AuthResponse issueAuthResponse(User user) {
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);

		RefreshToken tokenEntity = RefreshToken.builder()
				.user(user)
				.tokenHash(TokenHasher.sha256Hex(refreshToken))
				.expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpirationMs() / 1000))
				.build();
		refreshTokenRepository.save(tokenEntity);

		return new AuthResponse(
				accessToken,
				refreshToken,
				"Bearer",
				jwtService.getAccessTokenExpirationMs() / 1000,
				UserMapper.toResponse(user, resolveTenantId(user.getId())));
	}

	private UUID resolveTenantId(UUID userId) {
		return tenantUserRepository.findByUserId(userId)
				.map(tenantUser -> tenantUser.getTenant().getId())
				.orElse(null);
	}
}

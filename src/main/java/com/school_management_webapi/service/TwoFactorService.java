package com.school_management_webapi.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.response.TwoFactorResponse;
import com.school_management_webapi.entity.AuditAction;
import com.school_management_webapi.entity.TenantUser;
import com.school_management_webapi.entity.User;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.repository.TenantUserRepository;
import com.school_management_webapi.repository.UserRepository;
import com.school_management_webapi.security.TokenHasher;
import com.school_management_webapi.security.Totp;

import lombok.RequiredArgsConstructor;

/**
 * 67-security.md's two-factor sign-in, with an authenticator app.
 *
 * Setup is two calls so that nobody is switched on with a phone that never
 * scanned the code: the secret is handed out first, and two-factor is only in
 * force once a code made from it comes back. Recovery codes cover a lost phone;
 * an admin can reset someone who has lost both.
 */
@Service
@RequiredArgsConstructor
public class TwoFactorService {

	/** The name an authenticator app lists the account under. */
	private static final String ISSUER = "SchoolSuite";
	private static final int RECOVERY_CODE_COUNT = 8;
	private static final String RECOVERY_ALPHABET = "abcdefghjkmnpqrstuvwxyz23456789";
	/** Wrong codes allowed against one password check before it has to be repeated. */
	private static final int MAX_ATTEMPTS = 5;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final TenantUserRepository tenantUserRepository;
	private final TenantAuthorizationService tenantAuthorizationService;
	private final TenantSettingsService tenantSettingsService;
	private final PasswordEncoder passwordEncoder;
	private final AuditService auditService;

	/**
	 * Wrong codes per sign-in challenge. Six digits are a million guesses; a
	 * challenge lasts five minutes and allows five, so guessing is not a way in.
	 * Kept in memory: a restart only hands an attacker who already has the
	 * password a fresh five, and challenges expire on their own.
	 */
	private final Map<String, Integer> attemptsByChallenge = new ConcurrentHashMap<>();

	@Transactional(readOnly = true)
	public TwoFactorResponse.Status status(UUID userId) {
		User user = require(userId);
		return new TwoFactorResponse.Status(user.isTwoFactorEnabled(),
				tenantSettingsService.policyForUser(userId).isTwoFactorRequired(), user.getTwoFactorEnabledAt(),
				user.isTwoFactorEnabled() ? user.getRecoveryCodeHashes().size() : 0);
	}

	@Transactional
	public TwoFactorResponse.Setup beginSetup(UUID userId) {
		User user = require(userId);
		if (user.isTwoFactorEnabled()) {
			throw new ApiException(HttpStatus.CONFLICT, "TWO_FACTOR_ALREADY_ON",
					"Two-factor sign-in is already on. Turn it off first to move it to another phone.");
		}
		String secret = Totp.newSecret();
		user.setTotpSecret(secret);
		user.setTotpLastStep(null);
		userRepository.save(user);
		return new TwoFactorResponse.Setup(secret, Totp.otpauthUri(ISSUER, user.getEmail(), secret));
	}

	@Transactional
	public TwoFactorResponse.RecoveryCodes enable(UUID userId, String code) {
		User user = require(userId);
		if (user.isTwoFactorEnabled()) {
			throw new ApiException(HttpStatus.CONFLICT, "TWO_FACTOR_ALREADY_ON", "Two-factor sign-in is already on.");
		}
		if (user.getTotpSecret() == null) {
			throw new ApiException(HttpStatus.CONFLICT, "TWO_FACTOR_SETUP_NOT_STARTED",
					"Start the setup again to get a new QR code.");
		}
		if (!acceptAppCode(user, code)) {
			throw wrongCode();
		}
		user.setTwoFactorEnabledAt(LocalDateTime.now());
		List<String> codes = issueRecoveryCodes(user);
		userRepository.save(user);
		auditService.record(userId, AuditAction.TWO_FACTOR_ENABLED, null);
		return new TwoFactorResponse.RecoveryCodes(codes);
	}

	/** A fresh set, replacing the old; only an app code will do, so a recovery code cannot mint more. */
	@Transactional
	public TwoFactorResponse.RecoveryCodes regenerateRecoveryCodes(UUID userId, String code) {
		User user = requireEnabled(userId);
		if (!acceptAppCode(user, code)) {
			throw wrongCode();
		}
		List<String> codes = issueRecoveryCodes(user);
		userRepository.save(user);
		auditService.record(userId, AuditAction.RECOVERY_CODES_REGENERATED, null);
		return new TwoFactorResponse.RecoveryCodes(codes);
	}

	@Transactional
	public void disable(UUID userId, String password, String code) {
		User user = requireEnabled(userId);
		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", "That password is not right.");
		}
		if (tenantSettingsService.policyForUser(userId).isTwoFactorRequired()) {
			throw new ApiException(HttpStatus.CONFLICT, "TWO_FACTOR_REQUIRED",
					"Your school requires two-factor sign-in, so it cannot be turned off.");
		}
		if (!accept(user, code)) {
			throw wrongCode();
		}
		clear(user);
		userRepository.save(user);
		auditService.record(userId, AuditAction.TWO_FACTOR_DISABLED, null);
	}

	/**
	 * For someone who has lost their phone and their recovery codes: they sign
	 * in with the password alone next time, and set two-factor up again - at
	 * once, if the school requires it.
	 */
	@Transactional
	public void resetFor(UUID adminUserId, UUID targetUserId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(adminUserId);
		TenantUser membership = tenantUserRepository.findByTenantIdAndUserId(tenantId, targetUserId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));
		User target = membership.getUser();
		if (!target.isTwoFactorEnabled()) {
			throw new ApiException(HttpStatus.CONFLICT, "TWO_FACTOR_NOT_ON",
					target.getName() + " does not use two-factor sign-in.");
		}
		clear(target);
		userRepository.save(target);
		auditService.record(adminUserId, AuditAction.TWO_FACTOR_RESET, target.getName());
	}

	/**
	 * The second step of signing in: the app's current code, or an unused
	 * recovery code, which is then spent. A challenge that has succeeded, or
	 * failed too often, answers nothing more.
	 */
	@Transactional
	public boolean verifySignIn(User user, String challengeId, String code) {
		if (attemptsByChallenge.getOrDefault(challengeId, 0) >= MAX_ATTEMPTS) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "TWO_FACTOR_LOCKED",
					"Too many tries with that sign-in. Enter your password again.");
		}
		if (accept(user, code)) {
			attemptsByChallenge.put(challengeId, MAX_ATTEMPTS);
			userRepository.save(user);
			return true;
		}
		if (attemptsByChallenge.size() > 10_000) {
			attemptsByChallenge.clear();
		}
		attemptsByChallenge.merge(challengeId, 1, Integer::sum);
		return false;
	}

	private boolean accept(User user, String code) {
		return acceptAppCode(user, code) || acceptRecoveryCode(user, code);
	}

	/** Valid now, and newer than the last one accepted - a code seen over a shoulder is already spent. */
	private boolean acceptAppCode(User user, String code) {
		OptionalLong step = Totp.matchingStep(user.getTotpSecret(), code, System.currentTimeMillis() / 1000);
		if (step.isEmpty()) {
			return false;
		}
		Long last = user.getTotpLastStep();
		if (last != null && step.getAsLong() <= last) {
			return false;
		}
		user.setTotpLastStep(step.getAsLong());
		return true;
	}

	private boolean acceptRecoveryCode(User user, String code) {
		if (!user.isTwoFactorEnabled() || code == null) {
			return false;
		}
		return user.getRecoveryCodeHashes().remove(TokenHasher.sha256Hex(normalize(code)));
	}

	private List<String> issueRecoveryCodes(User user) {
		List<String> codes = new ArrayList<>();
		user.getRecoveryCodeHashes().clear();
		for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
			String code = randomChunk() + "-" + randomChunk();
			codes.add(code);
			user.getRecoveryCodeHashes().add(TokenHasher.sha256Hex(normalize(code)));
		}
		return codes;
	}

	private static String randomChunk() {
		StringBuilder chunk = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			chunk.append(RECOVERY_ALPHABET.charAt(RANDOM.nextInt(RECOVERY_ALPHABET.length())));
		}
		return chunk.toString();
	}

	/** Typed as shown, or without the dash, or in capitals - all the same code. */
	private static String normalize(String code) {
		return code.replaceAll("[\\s-]", "").toLowerCase();
	}

	private static void clear(User user) {
		user.setTotpSecret(null);
		user.setTwoFactorEnabledAt(null);
		user.setTotpLastStep(null);
		user.getRecoveryCodeHashes().clear();
	}

	private static ApiException wrongCode() {
		return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TWO_FACTOR_CODE",
				"That code is not right. Use the newest code in your app - and check your phone's clock is set automatically.");
	}

	private User require(UUID userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
	}

	private User requireEnabled(UUID userId) {
		User user = require(userId);
		if (!user.isTwoFactorEnabled()) {
			throw new ApiException(HttpStatus.CONFLICT, "TWO_FACTOR_NOT_ON", "Two-factor sign-in is not on.");
		}
		return user;
	}
}

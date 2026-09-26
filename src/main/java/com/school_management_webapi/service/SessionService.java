package com.school_management_webapi.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.response.SettingsResponse;
import com.school_management_webapi.entity.AuditAction;
import com.school_management_webapi.entity.RefreshToken;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

/**
 * Where a person is signed in, and ending it - 67-security.md's Active sessions.
 *
 * A session is one sign-in. Every token refresh replaces the refresh token but
 * keeps its session id, so the list shows sign-ins rather than tokens.
 */
@Service
@RequiredArgsConstructor
public class SessionService {

	/** A session's last-seen time is written at most this often, not on every request. */
	private static final long TOUCH_EVERY_SECONDS = 60;

	private final RefreshTokenRepository refreshTokenRepository;
	private final AuditService auditService;

	@Transactional(readOnly = true)
	public List<SettingsResponse.Session> list(UUID userId, UUID currentSessionId) {
		// Newest token per session; tokens that predate sessions stand alone.
		Map<UUID, RefreshToken> bySession = new LinkedHashMap<>();
		for (RefreshToken token : refreshTokenRepository.findActiveByUserId(userId, LocalDateTime.now())) {
			bySession.putIfAbsent(sessionOf(token), token);
		}
		return bySession.entrySet().stream()
				.map(entry -> {
					RefreshToken token = entry.getValue();
					return new SettingsResponse.Session(entry.getKey(),
							RequestInfo.describeDevice(token.getUserAgent()), token.getIpAddress(),
							token.getSessionStartedAt() != null ? token.getSessionStartedAt() : token.getCreatedAt(),
							token.getLastSeenAt() != null ? token.getLastSeenAt() : token.getCreatedAt(),
							entry.getKey().equals(currentSessionId));
				})
				.toList();
	}

	@Transactional
	public void revoke(UUID userId, UUID sessionId) {
		if (refreshTokenRepository.revokeSession(userId, sessionId, LocalDateTime.now()) == 0) {
			throw new ResourceNotFoundException("Session not found");
		}
		auditService.record(userId, AuditAction.SESSION_REVOKED, null);
	}

	/**
	 * True while the session has not been ended. Checked on every request, so a
	 * revoked session stops working at once rather than when its access token
	 * would have expired.
	 */
	@Transactional(readOnly = true)
	public boolean isActive(UUID sessionId) {
		return refreshTokenRepository.existsActiveSession(sessionId, LocalDateTime.now());
	}

	@Transactional
	public void touch(UUID sessionId) {
		LocalDateTime now = LocalDateTime.now();
		refreshTokenRepository.touchSession(sessionId, now, now.minusSeconds(TOUCH_EVERY_SECONDS));
	}

	public static UUID sessionOf(RefreshToken token) {
		return token.getSessionId() != null ? token.getSessionId() : token.getId();
	}
}

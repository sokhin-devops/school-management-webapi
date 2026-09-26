package com.school_management_webapi.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.school_management_webapi.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	private static final String CLAIM_TYPE = "type";
	private static final String TOKEN_TYPE_ACCESS = "access";
	private static final String TOKEN_TYPE_REFRESH = "refresh";
	/**
	 * Proof that the password was right, and nothing more: it opens no session
	 * and the authentication filter refuses it, so all it can do is be
	 * exchanged, with a code, for a real sign-in.
	 */
	private static final String TOKEN_TYPE_TWO_FACTOR = "two-factor";
	private static final long TWO_FACTOR_TOKEN_EXPIRATION_MS = 5 * 60 * 1000;
	/** The sign-in an access token belongs to, so a revoked session stops at once. */
	private static final String CLAIM_SESSION = "sid";

	private final SecretKey key;
	private final long accessTokenExpirationMs;
	private final long refreshTokenExpirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
			@Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpirationMs = accessTokenExpirationMs;
		this.refreshTokenExpirationMs = refreshTokenExpirationMs;
	}

	public String generateAccessToken(User user, UUID sessionId) {
		return buildToken(user, accessTokenExpirationMs, TOKEN_TYPE_ACCESS, sessionId);
	}

	public String generateRefreshToken(User user) {
		return buildToken(user, refreshTokenExpirationMs, TOKEN_TYPE_REFRESH, null);
	}

	public String generateTwoFactorToken(User user) {
		return buildToken(user, TWO_FACTOR_TOKEN_EXPIRATION_MS, TOKEN_TYPE_TWO_FACTOR, null);
	}

	public boolean isTwoFactorToken(String token) {
		return TOKEN_TYPE_TWO_FACTOR.equals(extractType(token));
	}

	/** The token's own id, which counts wrong codes against one challenge. */
	public String extractTokenId(String token) {
		return parseClaims(token).getId();
	}

	/** Null for a token issued before sessions were tracked. */
	public UUID extractSessionId(String token) {
		String sessionId = parseClaims(token).get(CLAIM_SESSION, String.class);
		return sessionId == null ? null : UUID.fromString(sessionId);
	}

	public long getAccessTokenExpirationMs() {
		return accessTokenExpirationMs;
	}

	public long getRefreshTokenExpirationMs() {
		return refreshTokenExpirationMs;
	}

	public boolean isValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	public boolean isAccessToken(String token) {
		return TOKEN_TYPE_ACCESS.equals(extractType(token));
	}

	public boolean isRefreshToken(String token) {
		return TOKEN_TYPE_REFRESH.equals(extractType(token));
	}

	public UUID extractUserId(String token) {
		return UUID.fromString(parseClaims(token).getSubject());
	}

	private String extractType(String token) {
		return parseClaims(token).get(CLAIM_TYPE, String.class);
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private String buildToken(User user, long expirationMs, String type, UUID sessionId) {
		Instant now = Instant.now();
		var builder = Jwts.builder();
		if (sessionId != null) {
			builder.claim(CLAIM_SESSION, sessionId.toString());
		}
		return builder
				// Everything else in a token is fixed per user and per second, so two
				// issued in the same second were identical - and the refresh token is
				// stored by its hash under a unique index, so the second sign-in failed
				// with 409. A random id makes every token its own.
				.id(UUID.randomUUID().toString())
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim(CLAIM_TYPE, type)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs)))
				.signWith(key)
				.compact();
	}
}

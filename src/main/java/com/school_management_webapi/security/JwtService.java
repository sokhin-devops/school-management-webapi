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

	public String generateAccessToken(User user) {
		return buildToken(user, accessTokenExpirationMs, TOKEN_TYPE_ACCESS);
	}

	public String generateRefreshToken(User user) {
		return buildToken(user, refreshTokenExpirationMs, TOKEN_TYPE_REFRESH);
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

	private String buildToken(User user, long expirationMs, String type) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim(CLAIM_TYPE, type)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs)))
				.signWith(key)
				.compact();
	}
}

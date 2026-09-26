package com.school_management_webapi.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.school_management_webapi.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	@Modifying
	@Query("UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.user.id = :userId AND r.revokedAt IS NULL")
	void revokeAllForUser(UUID userId, LocalDateTime now);

	/** The live token of every session this person has: one row per session. */
	@Query("SELECT r FROM RefreshToken r WHERE r.user.id = :userId AND r.revokedAt IS NULL AND r.expiresAt > :now ORDER BY r.lastSeenAt DESC NULLS LAST, r.createdAt DESC")
	List<RefreshToken> findActiveByUserId(UUID userId, LocalDateTime now);

	@Modifying
	@Query("UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.user.id = :userId AND r.revokedAt IS NULL AND (r.sessionId = :sessionId OR r.id = :sessionId)")
	int revokeSession(UUID userId, UUID sessionId, LocalDateTime now);

	/**
	 * Marks a session as used, at most once a minute: the WHERE clause makes
	 * every other request a lookup rather than a write.
	 */
	@Modifying
	@Query("UPDATE RefreshToken r SET r.lastSeenAt = :now WHERE r.sessionId = :sessionId AND r.revokedAt IS NULL AND (r.lastSeenAt IS NULL OR r.lastSeenAt < :threshold)")
	int touchSession(UUID sessionId, LocalDateTime now, LocalDateTime threshold);

	boolean existsByUserIdAndUserAgent(UUID userId, String userAgent);

	@Query("SELECT COUNT(r) > 0 FROM RefreshToken r WHERE (r.sessionId = :sessionId OR r.id = :sessionId) AND r.revokedAt IS NULL AND r.expiresAt > :now")
	boolean existsActiveSession(UUID sessionId, LocalDateTime now);

	boolean existsByUserId(UUID userId);
}

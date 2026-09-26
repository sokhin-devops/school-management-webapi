package com.school_management_webapi.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens", indexes = @Index(name = "ix_refresh_tokens_session", columnList = "session_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;

	/**
	 * The sign-in this token belongs to. A refresh revokes the old token and
	 * issues a new one; they share this id, which is what the Active sessions
	 * list shows and what Revoke ends. Null on tokens from before sessions were
	 * tracked, each of which then stands as a session of its own.
	 */
	@Column(name = "session_id")
	private UUID sessionId;

	@Column(name = "user_agent", length = 400)
	private String userAgent;

	@Column(name = "ip_address", length = 64)
	private String ipAddress;

	/** Last time an access token from this session was used; drives the idle timeout. */
	@Column(name = "last_seen_at")
	private LocalDateTime lastSeenAt;

	/** When the person signed in; carried across refreshes. */
	@Column(name = "session_started_at")
	private LocalDateTime sessionStartedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}

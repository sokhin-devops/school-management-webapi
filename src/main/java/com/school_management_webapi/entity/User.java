package com.school_management_webapi.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserStatus status;

	@Column(name = "email_verified_at")
	private LocalDateTime emailVerifiedAt;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	/** When the password was last set; a school's expiry policy counts from here. */
	@Column(name = "password_changed_at")
	private LocalDateTime passwordChangedAt;

	/**
	 * 67-security.md's two-factor sign-in: the secret shared with the person's
	 * authenticator app, base32. Set when setup starts; only in force once
	 * twoFactorEnabledAt is, so an abandoned setup never locks anyone out.
	 */
	@Column(name = "totp_secret", length = 64)
	private String totpSecret;

	@Column(name = "two_factor_enabled_at")
	private LocalDateTime twoFactorEnabledAt;

	/** The last 30-second step a code was accepted for, so one code signs in once. */
	@Column(name = "totp_last_step")
	private Long totpLastStep;

	/** One-time codes for a lost phone, kept only as hashes. */
	@ElementCollection
	@CollectionTable(name = "user_recovery_codes", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "code_hash", nullable = false, length = 64)
	@Builder.Default
	private Set<String> recoveryCodeHashes = new HashSet<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public boolean isTwoFactorEnabled() {
		return twoFactorEnabledAt != null;
	}
}

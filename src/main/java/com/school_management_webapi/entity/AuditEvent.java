package com.school_management_webapi.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One security-relevant thing that happened, for 67-security.md's audit log.
 *
 * Append-only: nothing updates or deletes these, so the log cannot be tidied
 * after the fact by the people it records. The actor's name is copied at the
 * time rather than joined, so the entry still reads correctly after they leave.
 */
@Entity
@Table(name = "audit_events", indexes = @Index(name = "ix_audit_events_tenant_time", columnList = "tenant_id, occurred_at"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	/** Null for an attempt nobody could be matched to, such as a wrong password. */
	@Column(name = "actor_user_id")
	private UUID actorUserId;

	@Column(name = "actor_name", nullable = false, length = 150)
	private String actorName;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, length = 40)
	private AuditAction action;

	@Column(name = "detail", length = 500)
	private String detail;

	@Column(name = "ip_address", length = 64)
	private String ipAddress;

	@CreationTimestamp
	@Column(name = "occurred_at", nullable = false, updatable = false)
	private LocalDateTime occurredAt;
}

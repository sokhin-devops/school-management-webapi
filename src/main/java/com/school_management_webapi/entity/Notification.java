package com.school_management_webapi.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One in-app notification for one person - what the topbar's bell lists. */
@Entity
@Table(name = "notifications",
		indexes = @Index(name = "ix_notifications_recipient_time", columnList = "recipient_user_id, created_at"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "recipient_user_id", nullable = false)
	private UUID recipientUserId;

	@Column(name = "event_key", nullable = false, length = 40)
	private String eventKey;

	@Column(name = "title", nullable = false, length = 150)
	private String title;

	@Column(name = "message", length = 500)
	private String message;

	/** Where in the web app the notification is about, e.g. "/finance/payments". */
	@Column(name = "link", length = 200)
	private String link;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "read_at")
	private LocalDateTime readAt;
}

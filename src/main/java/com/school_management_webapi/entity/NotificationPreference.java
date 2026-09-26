package com.school_management_webapi.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One person's choice for one event, per channel - 65-notifications.md.
 *
 * Only choices that differ from the default are stored; an event with no row
 * is delivered in-app, which is what everyone starts with.
 */
@Entity
@Table(name = "notification_preferences",
		uniqueConstraints = @UniqueConstraint(name = "uk_notification_preferences_user_event",
				columnNames = { "user_id", "event_key" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "event_key", nullable = false, length = 40)
	private String eventKey;

	@Column(name = "in_app", nullable = false)
	private boolean inApp;

	/** Stored so the choice survives until email delivery exists; nothing sends mail yet. */
	@Column(name = "email", nullable = false)
	private boolean email;
}

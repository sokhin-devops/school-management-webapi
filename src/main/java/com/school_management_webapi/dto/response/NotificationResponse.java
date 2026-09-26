package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** The bell's list, its count, and the preferences screen. */
public final class NotificationResponse {

	private NotificationResponse() {
	}

	public record Item(
			UUID id,
			String eventKey,
			String title,
			String message,
			String link,
			LocalDateTime createdAt,
			boolean read) {
	}

	public record Inbox(long unread, List<Item> items) {
	}

	/** One event someone can be told about, with their current choice per channel. */
	public record Preference(String key, String title, String note, boolean inApp, boolean email) {
	}

	public record PreferenceGroup(String key, String title, String description, List<Preference> preferences) {
	}

	/** The catalogue comes from the server, so the screen can never list an event nothing raises. */
	public record Preferences(List<PreferenceGroup> groups, boolean emailAvailable) {
	}
}

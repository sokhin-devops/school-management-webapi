package com.school_management_webapi.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Every event the system can tell someone about - 65-notifications.md.
 *
 * Only events something actually raises are listed. The preferences screen is
 * drawn from this catalogue, so it can never offer a switch for an event that
 * never happens. Each belongs to a module of the role grid: a person is only
 * told about what their role lets them see.
 */
public enum NotificationEvent {

	ATTENDANCE_SUBMITTED("academic", "attendance", "Attendance submitted",
			"A register is completed for a class."),
	GRADES_PUBLISHED("academic", "academic", "Grades published",
			"Marks are saved for an assessment."),
	YEAR_ROLLED_OVER("academic", "academic", "Academic year rolled over",
			"The active year ends and the next one becomes active."),

	PAYMENT_RECEIVED("finance", "finance", "Payment received",
			"A payment is recorded against a fee."),
	EXPENSE_SUBMITTED("finance", "finance", "Expense awaiting approval",
			"Someone records an expense that is still pending."),

	STUDENT_ENROLLED("people", "students", "Student enrolled",
			"A student record is created."),
	STAFF_CHANGED("people", "teachers", "Staff record changed",
			"A teacher is added or their record is edited."),

	USER_INVITED("system", "settings", "User invited",
			"Someone is invited to the school."),
	ROLE_CHANGED("system", "settings", "Role or permissions changed",
			"A role is created, edited or deleted."),
	NEW_DEVICE_SIGN_IN("system", null, "Sign-in from a new device",
			"Your account is used from a browser it has not signed in from before."),
	MAINTENANCE("system", null, "Maintenance mode",
			"The school is taken offline for maintenance, or brought back.");

	public record Group(String key, String title, String description) {
	}

	public static final List<Group> GROUPS = List.of(
			new Group("academic", "Academic", "Classes, attendance and results."),
			new Group("finance", "Finance", "Payments and spending."),
			new Group("people", "People", "Students and staff."),
			new Group("system", "System", "Accounts, access and service notices."));

	private final String group;
	/** Null for an event about the person themselves, or the whole school. */
	private final String module;
	private final String title;
	private final String note;

	NotificationEvent(String group, String module, String title, String note) {
		this.group = group;
		this.module = module;
		this.title = title;
		this.note = note;
	}

	public String key() {
		return name().toLowerCase();
	}

	public String group() {
		return group;
	}

	public String module() {
		return module;
	}

	public String title() {
		return title;
	}

	public String note() {
		return note;
	}

	public static Optional<NotificationEvent> fromKey(String key) {
		return Arrays.stream(values()).filter(event -> event.key().equals(key)).findFirst();
	}
}

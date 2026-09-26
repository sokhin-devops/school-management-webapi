package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.AuditAction;
import com.school_management_webapi.entity.BillingCycle;
import com.school_management_webapi.entity.GradingScale;
import com.school_management_webapi.entity.TermStructure;

/** What Settings reads back, one record per section. */
public final class SettingsResponse {

	private SettingsResponse() {
	}

	public record Security(
			int passwordMinLength,
			boolean passwordRequireUppercase,
			boolean passwordRequireNumber,
			boolean passwordRequireSymbol,
			int passwordExpiryDays,
			boolean signOutOnPasswordChange,
			int sessionTimeoutMinutes,
			boolean twoFactorRequired,
			/** Whether two-factor sign-in can be required yet; the screen hides the switch when not. */
			boolean twoFactorAvailable) {
	}

	public record Academic(
			boolean usePrograms,
			boolean useLevels,
			boolean useSections,
			boolean useSubjects,
			boolean useTerms,
			boolean useRooms,
			String levelLabel,
			String classLabel,
			String subjectLabel,
			String studentLabel,
			String teacherLabel,
			boolean autoRollover,
			TermStructure termStructure,
			GradingScale gradingScale,
			int passMark) {
	}

	public record SystemSettings(boolean maintenanceMode) {
	}

	public record Billing(String billingEmail, String billingAddress) {
	}

	/** What this installation is running - read-only facts, all of them real. */
	public record SystemInfo(
			String version,
			String environment,
			String database,
			LocalDateTime startedAt,
			LocalDateTime serverTime) {
	}

	public record AuditEntry(
			UUID id,
			String actorName,
			AuditAction action,
			String detail,
			String ipAddress,
			LocalDateTime occurredAt) {
	}

	public record Session(
			UUID id,
			String device,
			String ipAddress,
			LocalDateTime startedAt,
			LocalDateTime lastSeenAt,
			boolean current) {
	}

	/** One limit the plan sells, against what is used. A null limit is unlimited. */
	public record UsageRow(String key, String label, long used, Integer limit) {
	}

	public record Usage(String planName, List<UsageRow> rows) {
	}

	public record Invoice(
			UUID id,
			String number,
			String planName,
			BillingCycle billingCycle,
			BigDecimal amount,
			String currency,
			LocalDateTime periodStart,
			LocalDateTime periodEnd,
			LocalDateTime issuedAt,
			String billToName,
			String billToEmail,
			String billToAddress) {
	}
}

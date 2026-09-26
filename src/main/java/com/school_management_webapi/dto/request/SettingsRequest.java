package com.school_management_webapi.dto.request;

import com.school_management_webapi.entity.GradingScale;
import com.school_management_webapi.entity.TermStructure;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The bodies Settings sends, one per section it saves. */
public final class SettingsRequest {

	private SettingsRequest() {
	}

	/** 67-security.md. */
	public record Security(
			@Min(value = 8, message = "passwordMinLength must be at least 8") @Max(value = 64, message = "passwordMinLength must be at most 64") int passwordMinLength,
			boolean passwordRequireUppercase,
			boolean passwordRequireNumber,
			boolean passwordRequireSymbol,
			@Min(value = 0, message = "passwordExpiryDays cannot be negative") @Max(value = 3650, message = "passwordExpiryDays must be at most 3650") int passwordExpiryDays,
			boolean signOutOnPasswordChange,
			@Min(value = 15, message = "sessionTimeoutMinutes must be at least 15") @Max(value = 10080, message = "sessionTimeoutMinutes must be at most 10080") int sessionTimeoutMinutes,
			boolean twoFactorRequired) {
	}

	/** 63-academic-settings.md. */
	public record Academic(
			boolean usePrograms,
			boolean useLevels,
			boolean useSections,
			boolean useSubjects,
			boolean useTerms,
			boolean useRooms,
			@NotBlank(message = "levelLabel is required") @Size(max = 40, message = "levelLabel must be at most 40 characters") String levelLabel,
			@NotBlank(message = "classLabel is required") @Size(max = 40, message = "classLabel must be at most 40 characters") String classLabel,
			@NotBlank(message = "subjectLabel is required") @Size(max = 40, message = "subjectLabel must be at most 40 characters") String subjectLabel,
			@NotBlank(message = "studentLabel is required") @Size(max = 40, message = "studentLabel must be at most 40 characters") String studentLabel,
			@NotBlank(message = "teacherLabel is required") @Size(max = 40, message = "teacherLabel must be at most 40 characters") String teacherLabel,
			boolean autoRollover,
			@NotNull(message = "termStructure is required") TermStructure termStructure,
			@NotNull(message = "gradingScale is required") GradingScale gradingScale,
			@Min(value = 1, message = "passMark must be at least 1") @Max(value = 100, message = "passMark must be at most 100") int passMark) {
	}

	/** 69-system-settings.md. */
	public record SystemSettings(boolean maintenanceMode) {
	}

	/** 68-subscription.md: where invoices go. */
	public record Billing(
			@NotBlank(message = "billingEmail is required") @Email(message = "billingEmail must be a valid email address") @Size(max = 255, message = "billingEmail must be at most 255 characters") String billingEmail,
			@Size(max = 1000, message = "billingAddress must be at most 1000 characters") String billingAddress) {
	}

	/** Deleting everything asks for the school's name, typed, as the confirmation. */
	public record DeleteAllData(@NotBlank(message = "confirmation is required") String confirmation) {
	}
}

package com.school_management_webapi.dto.response;

/**
 * Result of an onboarding step. The steps are idempotent - the "Back" button in
 * the setup wizard means a step can be submitted more than once - so callers
 * need to know whether the record was created or an earlier answer was revised.
 */
public record OnboardingStepResult<T>(T data, boolean created) {

	public static <T> OnboardingStepResult<T> created(T data) {
		return new OnboardingStepResult<>(data, true);
	}

	public static <T> OnboardingStepResult<T> updated(T data) {
		return new OnboardingStepResult<>(data, false);
	}
}

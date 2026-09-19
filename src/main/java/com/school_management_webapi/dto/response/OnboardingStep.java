package com.school_management_webapi.dto.response;

/**
 * The step the tenant still has to complete. Drives the setup stepper in the
 * frontend so a half-finished onboarding resumes where it left off.
 */
public enum OnboardingStep {
	SUBSCRIPTION,
	SCHOOL,
	BRANCH,
	ACADEMIC_YEAR,
	COMPLETE
}

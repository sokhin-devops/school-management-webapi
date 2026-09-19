package com.school_management_webapi.dto.response;

public record OnboardingStatusResponse(
		boolean subscriptionCompleted,
		boolean schoolCompleted,
		boolean branchCompleted,
		boolean academicYearCompleted,
		boolean completed,
		OnboardingStep currentStep,
		SchoolResponse school,
		BranchResponse branch,
		AcademicYearResponse academicYear) {
}

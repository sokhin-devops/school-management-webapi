package com.school_management_webapi.dto.response;

public record OnboardingStatusResponse(
		boolean schoolCompleted,
		boolean branchCompleted,
		boolean academicYearCompleted,
		boolean completed,
		SchoolResponse school,
		BranchResponse branch,
		AcademicYearResponse academicYear) {
}

package com.school_management_webapi.service;

import java.util.UUID;

import com.school_management_webapi.dto.request.OnboardingAcademicYearRequest;
import com.school_management_webapi.dto.request.OnboardingBranchRequest;
import com.school_management_webapi.dto.request.SchoolRequest;
import com.school_management_webapi.dto.response.AcademicYearResponse;
import com.school_management_webapi.dto.response.BranchResponse;
import com.school_management_webapi.dto.response.OnboardingStatusResponse;
import com.school_management_webapi.dto.response.SchoolResponse;

public interface OnboardingService {

	OnboardingStatusResponse getStatus(UUID userId);

	SchoolResponse createSchool(UUID userId, SchoolRequest request);

	BranchResponse createBranch(UUID userId, OnboardingBranchRequest request);

	AcademicYearResponse createAcademicYear(UUID userId, OnboardingAcademicYearRequest request);
}

package com.school_management_webapi.service;

import java.util.UUID;

import com.school_management_webapi.dto.request.OnboardingAcademicYearRequest;
import com.school_management_webapi.dto.request.OnboardingBranchRequest;
import com.school_management_webapi.dto.request.SchoolRequest;
import com.school_management_webapi.dto.response.AcademicYearResponse;
import com.school_management_webapi.dto.response.BranchResponse;
import com.school_management_webapi.dto.response.OnboardingStatusResponse;
import com.school_management_webapi.dto.response.OnboardingStepResult;
import com.school_management_webapi.dto.response.SchoolResponse;

public interface OnboardingService {

	OnboardingStatusResponse getStatus(UUID userId);

	OnboardingStepResult<SchoolResponse> saveSchool(UUID userId, SchoolRequest request);

	OnboardingStepResult<BranchResponse> saveBranch(UUID userId, OnboardingBranchRequest request);

	OnboardingStepResult<AcademicYearResponse> saveAcademicYear(UUID userId, OnboardingAcademicYearRequest request);
}

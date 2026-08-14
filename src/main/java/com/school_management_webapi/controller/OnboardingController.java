package com.school_management_webapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.OnboardingAcademicYearRequest;
import com.school_management_webapi.dto.request.OnboardingBranchRequest;
import com.school_management_webapi.dto.request.SchoolRequest;
import com.school_management_webapi.dto.response.AcademicYearResponse;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.BranchResponse;
import com.school_management_webapi.dto.response.OnboardingStatusResponse;
import com.school_management_webapi.dto.response.SchoolResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.OnboardingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Tag(name = "Onboarding")
public class OnboardingController {

	private final OnboardingService onboardingService;

	@GetMapping("/status")
	@Operation(summary = "Get the tenant's onboarding progress")
	public ResponseEntity<ApiResponse<OnboardingStatusResponse>> status(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Onboarding status retrieved",
				onboardingService.getStatus(principal.getUser().getId())));
	}

	@PostMapping("/school")
	@Operation(summary = "Onboarding step 1: create the tenant's first school")
	public ResponseEntity<ApiResponse<SchoolResponse>> school(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody SchoolRequest request) {
		SchoolResponse response = onboardingService.createSchool(principal.getUser().getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("School setup completed", response));
	}

	@PostMapping("/branch")
	@Operation(summary = "Onboarding step 2: create the first branch for the tenant's school")
	public ResponseEntity<ApiResponse<BranchResponse>> branch(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody OnboardingBranchRequest request) {
		BranchResponse response = onboardingService.createBranch(principal.getUser().getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Branch setup completed", response));
	}

	@PostMapping("/academic-year")
	@Operation(summary = "Onboarding step 3: create the first academic year and complete setup")
	public ResponseEntity<ApiResponse<AcademicYearResponse>> academicYear(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody OnboardingAcademicYearRequest request) {
		AcademicYearResponse response = onboardingService.createAcademicYear(principal.getUser().getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Onboarding complete", response));
	}
}

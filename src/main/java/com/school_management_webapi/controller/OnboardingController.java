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
import com.school_management_webapi.dto.response.OnboardingStepResult;
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
	@Operation(summary = "Get the onboarding progress of the tenant and the step to resume from")
	public ResponseEntity<ApiResponse<OnboardingStatusResponse>> status(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Onboarding status retrieved",
				onboardingService.getStatus(principal.getUser().getId())));
	}

	@PostMapping("/school")
	@Operation(summary = "Onboarding step 1: create or revise the school of the tenant")
	public ResponseEntity<ApiResponse<SchoolResponse>> school(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody SchoolRequest request) {
		return respond(onboardingService.saveSchool(principal.getUser().getId(), request), "School setup completed",
				"School setup updated");
	}

	@PostMapping("/branch")
	@Operation(summary = "Onboarding step 2: create or revise the first branch of the school")
	public ResponseEntity<ApiResponse<BranchResponse>> branch(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody OnboardingBranchRequest request) {
		return respond(onboardingService.saveBranch(principal.getUser().getId(), request), "Branch setup completed",
				"Branch setup updated");
	}

	@PostMapping("/academic-year")
	@Operation(summary = "Onboarding step 3: create or revise the first academic year and complete setup")
	public ResponseEntity<ApiResponse<AcademicYearResponse>> academicYear(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody OnboardingAcademicYearRequest request) {
		return respond(onboardingService.saveAcademicYear(principal.getUser().getId(), request), "Onboarding complete",
				"Academic year setup updated");
	}

	/**
	 * 201 the first time a step is answered, 200 when the wizard comes back to a
	 * step that was already answered.
	 */
	private <T> ResponseEntity<ApiResponse<T>> respond(OnboardingStepResult<T> result, String createdMessage,
			String updatedMessage) {
		HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
		String message = result.created() ? createdMessage : updatedMessage;
		return ResponseEntity.status(status).body(ApiResponse.success(message, result.data()));
	}
}

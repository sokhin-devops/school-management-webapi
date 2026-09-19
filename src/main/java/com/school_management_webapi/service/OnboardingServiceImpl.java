package com.school_management_webapi.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.AcademicYearRequest;
import com.school_management_webapi.dto.request.BranchRequest;
import com.school_management_webapi.dto.request.OnboardingAcademicYearRequest;
import com.school_management_webapi.dto.request.OnboardingBranchRequest;
import com.school_management_webapi.dto.request.SchoolRequest;
import com.school_management_webapi.dto.response.AcademicYearResponse;
import com.school_management_webapi.dto.response.BranchResponse;
import com.school_management_webapi.dto.response.OnboardingStatusResponse;
import com.school_management_webapi.dto.response.OnboardingStep;
import com.school_management_webapi.dto.response.OnboardingStepResult;
import com.school_management_webapi.dto.response.SchoolResponse;
import com.school_management_webapi.entity.AcademicYear;
import com.school_management_webapi.entity.Branch;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.mapper.AcademicYearMapper;
import com.school_management_webapi.mapper.BranchMapper;
import com.school_management_webapi.mapper.SchoolMapper;
import com.school_management_webapi.repository.AcademicYearRepository;
import com.school_management_webapi.repository.BranchRepository;
import com.school_management_webapi.repository.SchoolRepository;

import lombok.RequiredArgsConstructor;

/**
 * Drives the three-step setup wizard (school, branch, academic year) that runs
 * once a tenant has picked a plan.
 *
 * <p>
 * Every step is an upsert against the first school of the tenant and that
 * first branch / academic year of that school. The wizard has a Back button, so
 * the same step is routinely submitted twice; re-submitting revises the earlier
 * answer instead of piling up duplicate records.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingServiceImpl implements OnboardingService {

	private final TenantAuthorizationService tenantAuthorizationService;
	private final CurrentSubscriptionResolver currentSubscriptionResolver;
	private final SchoolRepository schoolRepository;
	private final BranchRepository branchRepository;
	private final AcademicYearRepository academicYearRepository;
	private final SchoolService schoolService;
	private final BranchService branchService;
	private final AcademicYearService academicYearService;

	@Override
	@Transactional(readOnly = true)
	public OnboardingStatusResponse getStatus(UUID userId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);

		boolean subscriptionDone = currentSubscriptionResolver.hasActiveSubscription(tenantId);

		School school = schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(tenantId).orElse(null);
		Branch branch = school != null
				? branchRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId()).orElse(null)
				: null;
		AcademicYear academicYear = school != null
				? academicYearRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId()).orElse(null)
				: null;

		boolean schoolDone = school != null;
		boolean branchDone = branch != null;
		boolean academicYearDone = academicYear != null;
		boolean completed = subscriptionDone && schoolDone && branchDone && academicYearDone;

		return new OnboardingStatusResponse(
				subscriptionDone,
				schoolDone,
				branchDone,
				academicYearDone,
				completed,
				resolveCurrentStep(subscriptionDone, schoolDone, branchDone, academicYearDone),
				school != null ? SchoolMapper.toResponse(school) : null,
				branch != null ? BranchMapper.toResponse(branch) : null,
				academicYear != null ? AcademicYearMapper.toResponse(academicYear) : null);
	}

	@Override
	public OnboardingStepResult<SchoolResponse> saveSchool(UUID userId, SchoolRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		currentSubscriptionResolver.resolveActive(tenantId);

		Optional<School> existing = schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(tenantId);
		if (existing.isPresent()) {
			return OnboardingStepResult.updated(schoolService.update(userId, existing.get().getId(), request));
		}
		return OnboardingStepResult.created(schoolService.create(userId, request));
	}

	@Override
	public OnboardingStepResult<BranchResponse> saveBranch(UUID userId, OnboardingBranchRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		School school = requireSchool(tenantId, "Complete school setup before adding a branch.");

		BranchRequest branchRequest = new BranchRequest(school.getId(), request.name(), request.address(),
				request.phone(), true);

		Optional<Branch> existing = branchRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId());
		if (existing.isPresent()) {
			return OnboardingStepResult.updated(branchService.update(userId, existing.get().getId(), branchRequest));
		}
		return OnboardingStepResult.created(branchService.create(userId, branchRequest));
	}

	@Override
	public OnboardingStepResult<AcademicYearResponse> saveAcademicYear(UUID userId,
			OnboardingAcademicYearRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		School school = requireSchool(tenantId, "Complete school setup before adding an academic year.");

		if (!branchRepository.existsBySchoolId(school.getId())) {
			throw new ApiException(HttpStatus.CONFLICT, "BRANCH_REQUIRED",
					"Complete branch setup before adding an academic year.");
		}

		AcademicYearRequest yearRequest = new AcademicYearRequest(school.getId(), request.name(), request.startDate(),
				request.endDate(), true);

		Optional<AcademicYear> existing = academicYearRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId());
		if (existing.isPresent()) {
			return OnboardingStepResult
					.updated(academicYearService.update(userId, existing.get().getId(), yearRequest));
		}
		return OnboardingStepResult.created(academicYearService.create(userId, yearRequest));
	}

	private OnboardingStep resolveCurrentStep(boolean subscriptionDone, boolean schoolDone, boolean branchDone,
			boolean academicYearDone) {
		if (!subscriptionDone) {
			return OnboardingStep.SUBSCRIPTION;
		}
		if (!schoolDone) {
			return OnboardingStep.SCHOOL;
		}
		if (!branchDone) {
			return OnboardingStep.BRANCH;
		}
		if (!academicYearDone) {
			return OnboardingStep.ACADEMIC_YEAR;
		}
		return OnboardingStep.COMPLETE;
	}

	private School requireSchool(UUID tenantId, String message) {
		return schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "SCHOOL_REQUIRED", message));
	}
}

package com.school_management_webapi.service;

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

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingServiceImpl implements OnboardingService {

	private final TenantAuthorizationService tenantAuthorizationService;
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

		return new OnboardingStatusResponse(
				schoolDone,
				branchDone,
				academicYearDone,
				schoolDone && branchDone && academicYearDone,
				school != null ? SchoolMapper.toResponse(school) : null,
				branch != null ? BranchMapper.toResponse(branch) : null,
				academicYear != null ? AcademicYearMapper.toResponse(academicYear) : null);
	}

	@Override
	public SchoolResponse createSchool(UUID userId, SchoolRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		if (schoolRepository.existsByTenantId(tenantId)) {
			throw new ApiException(HttpStatus.CONFLICT, "SCHOOL_ALREADY_EXISTS",
					"A school already exists for this tenant. Use /api/v1/schools to add additional schools.");
		}
		return schoolService.create(userId, request);
	}

	@Override
	public BranchResponse createBranch(UUID userId, OnboardingBranchRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		School school = schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "SCHOOL_REQUIRED",
						"Complete school setup before adding a branch."));

		return branchService.create(userId,
				new BranchRequest(school.getId(), request.name(), request.address(), request.phone(), true));
	}

	@Override
	public AcademicYearResponse createAcademicYear(UUID userId, OnboardingAcademicYearRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		School school = schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "SCHOOL_REQUIRED",
						"Complete school setup before adding an academic year."));

		if (!branchRepository.existsBySchoolId(school.getId())) {
			throw new ApiException(HttpStatus.CONFLICT, "BRANCH_REQUIRED",
					"Complete branch setup before adding an academic year.");
		}

		return academicYearService.create(userId,
				new AcademicYearRequest(school.getId(), request.name(), request.startDate(), request.endDate(),
						true));
	}
}

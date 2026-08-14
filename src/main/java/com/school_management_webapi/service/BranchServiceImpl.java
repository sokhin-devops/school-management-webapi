package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.BranchRequest;
import com.school_management_webapi.dto.response.BranchResponse;
import com.school_management_webapi.entity.Branch;
import com.school_management_webapi.entity.BranchStatus;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.mapper.BranchMapper;
import com.school_management_webapi.repository.BranchRepository;
import com.school_management_webapi.repository.SchoolRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchServiceImpl implements BranchService {

	private final BranchRepository branchRepository;
	private final SchoolRepository schoolRepository;
	private final TenantAuthorizationService tenantAuthorizationService;

	@Override
	public BranchResponse create(UUID userId, BranchRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		School school = resolveSchoolForTenant(request.schoolId(), tenantId);

		boolean makeMain = request.mainBranch() || !branchRepository.existsBySchoolId(school.getId());
		if (makeMain) {
			unsetExistingMainBranch(school.getId());
		}

		Branch branch = Branch.builder()
				.school(school)
				.name(request.name())
				.address(request.address())
				.phone(request.phone())
				.mainBranch(makeMain)
				.status(BranchStatus.ACTIVE)
				.build();

		return BranchMapper.toResponse(branchRepository.save(branch));
	}

	@Override
	@Transactional(readOnly = true)
	public List<BranchResponse> list(UUID userId, UUID schoolId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		List<Branch> branches = schoolId != null
				? branchRepository.findBySchoolIdOrderByCreatedAtAsc(resolveSchoolForTenant(schoolId, tenantId).getId())
				: branchRepository.findAllByTenantId(tenantId);
		return branches.stream().map(BranchMapper::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public BranchResponse getById(UUID userId, UUID branchId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return BranchMapper.toResponse(findBranchOrThrow(branchId, tenantId));
	}

	@Override
	public BranchResponse update(UUID userId, UUID branchId, BranchRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		Branch branch = findBranchOrThrow(branchId, tenantId);

		if (request.mainBranch()) {
			unsetExistingMainBranch(branch.getSchool().getId());
		}

		branch.setName(request.name());
		branch.setAddress(request.address());
		branch.setPhone(request.phone());
		branch.setMainBranch(request.mainBranch());

		return BranchMapper.toResponse(branchRepository.save(branch));
	}

	@Override
	public void delete(UUID userId, UUID branchId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		branchRepository.delete(findBranchOrThrow(branchId, tenantId));
	}

	private Branch findBranchOrThrow(UUID branchId, UUID tenantId) {
		return branchRepository.findByIdAndTenantId(branchId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BRANCH_NOT_FOUND", "Branch not found."));
	}

	private School resolveSchoolForTenant(UUID schoolId, UUID tenantId) {
		return schoolRepository.findByIdAndTenantId(schoolId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SCHOOL_NOT_FOUND", "School not found."));
	}

	private void unsetExistingMainBranch(UUID schoolId) {
		branchRepository.findBySchoolIdOrderByCreatedAtAsc(schoolId).stream()
				.filter(Branch::isMainBranch)
				.forEach(existing -> {
					existing.setMainBranch(false);
					branchRepository.save(existing);
				});
	}
}

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
	private final SubscriptionLimitService subscriptionLimitService;

	@Override
	public BranchResponse create(UUID userId, BranchRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		School school = resolveSchoolForTenant(request.schoolId(), tenantId);

		// Resolves the live subscription as a side effect, so a tenant without one
		// gets SUBSCRIPTION_REQUIRED rather than an unmetered branch.
		subscriptionLimitService.checkLimit(tenantId, LimitType.BRANCHES, branchRepository.countByTenantId(tenantId));

		String name = request.name().trim();
		ensureBranchNameIsFree(school.getId(), name, null);

		boolean makeMain = request.mainBranch() || !branchRepository.existsBySchoolId(school.getId());
		if (makeMain) {
			unsetExistingMainBranch(school.getId(), null);
		}

		Branch branch = Branch.builder()
				.school(school)
				.name(name)
				.address(request.address().trim())
				.phone(normalizePhone(request.phone()))
				.mainBranch(makeMain)
				.status(request.status() != null ? request.status() : BranchStatus.ACTIVE)
				.build();

		return BranchMapper.toResponse(branchRepository.saveAndFlush(branch));
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
		UUID schoolId = branch.getSchool().getId();

		String name = request.name().trim();
		ensureBranchNameIsFree(schoolId, name, branch.getId());

		if (request.mainBranch()) {
			unsetExistingMainBranch(schoolId, branch.getId());
		} else if (branch.isMainBranch()) {
			// Every school keeps exactly one main branch; clearing the flag has to go
			// through promoting a different branch instead.
			throw new ApiException(HttpStatus.CONFLICT, "MAIN_BRANCH_REQUIRED",
					"A school must have one main branch. Mark another branch as main instead.");
		}

		branch.setName(name);
		branch.setAddress(request.address().trim());
		branch.setPhone(normalizePhone(request.phone()));
		branch.setMainBranch(request.mainBranch());
		if (request.status() != null) {
			branch.setStatus(request.status());
		}

		return BranchMapper.toResponse(branchRepository.saveAndFlush(branch));
	}

	@Override
	public void delete(UUID userId, UUID branchId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		Branch branch = findBranchOrThrow(branchId, tenantId);
		UUID schoolId = branch.getSchool().getId();

		List<Branch> others = branchRepository.findBySchoolIdOrderByCreatedAtAsc(schoolId).stream()
				.filter(remaining -> !remaining.getId().equals(branch.getId()))
				.toList();

		// Every record in the system is scoped to a branch, and a new branch takes
		// its school from an existing one - a school left with none cannot be
		// worked in, or even given a branch again, from the app.
		if (others.isEmpty()) {
			throw new ApiException(HttpStatus.CONFLICT, "LAST_BRANCH",
					"A school needs at least one branch. Add another before deleting this one.");
		}

		// Resolved before the delete so the promotion target is never the row that is
		// on its way out of the persistence context.
		Branch promoted = branch.isMainBranch() ? others.get(0) : null;

		branchRepository.delete(branch);

		if (promoted != null) {
			promoted.setMainBranch(true);
			branchRepository.save(promoted);
		}
	}

	private Branch findBranchOrThrow(UUID branchId, UUID tenantId) {
		return branchRepository.findByIdAndTenantId(branchId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BRANCH_NOT_FOUND", "Branch not found."));
	}

	private School resolveSchoolForTenant(UUID schoolId, UUID tenantId) {
		return schoolRepository.findByIdAndTenantId(schoolId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SCHOOL_NOT_FOUND", "School not found."));
	}

	private void ensureBranchNameIsFree(UUID schoolId, String name, UUID excludedBranchId) {
		branchRepository.findBySchoolIdAndNameIgnoreCase(schoolId, name)
				.filter(existing -> !existing.getId().equals(excludedBranchId))
				.ifPresent(existing -> {
					throw new ApiException(HttpStatus.CONFLICT, "BRANCH_NAME_ALREADY_EXISTS",
							"A branch named '" + name + "' already exists for this school.");
				});
	}

	private void unsetExistingMainBranch(UUID schoolId, UUID excludedBranchId) {
		branchRepository.findBySchoolIdOrderByCreatedAtAsc(schoolId).stream()
				.filter(Branch::isMainBranch)
				.filter(existing -> !existing.getId().equals(excludedBranchId))
				.forEach(existing -> {
					existing.setMainBranch(false);
					branchRepository.save(existing);
				});
	}

	private String normalizePhone(String phone) {
		return phone == null || phone.isBlank() ? null : phone.trim();
	}
}

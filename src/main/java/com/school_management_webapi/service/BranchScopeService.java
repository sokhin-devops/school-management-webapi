package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.BranchRepository;

import lombok.RequiredArgsConstructor;

/**
 * Where every branch-scoped read and write starts.
 *
 * The academic, finance and attendance modules all hang off a branch, so the
 * same three questions - which tenant is calling, which branches may it see, is
 * this branch one of them - are asked once here instead of in each service.
 */
@Service
@RequiredArgsConstructor
public class BranchScopeService {

	private final TenantAuthorizationService tenantAuthorizationService;
	private final BranchRepository branchRepository;

	public UUID requireTenantId(UUID userId) {
		return tenantAuthorizationService.requireTenantId(userId);
	}

	/**
	 * Every branch the tenant owns. An empty list is a legitimate answer for a
	 * tenant that has not finished onboarding, and it matches no rows - which is
	 * the right result rather than an error.
	 */
	public List<UUID> allowedBranchIds(UUID tenantId) {
		return branchRepository.findIdsByTenantId(tenantId);
	}

	/**
	 * A branch outside the caller's tenant is reported as missing rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	public void requireBranchInTenant(UUID branchId, UUID tenantId) {
		if (!branchRepository.existsByIdAndTenantId(branchId, tenantId)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "BRANCH_NOT_FOUND", "Branch not found.");
		}
	}
}

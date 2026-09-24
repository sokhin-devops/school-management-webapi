package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.FeeCreateRequest;
import com.school_management_webapi.dto.request.FeePatchRequest;
import com.school_management_webapi.dto.request.FeeUpdateRequest;
import com.school_management_webapi.dto.response.FeeResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.Fee;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.FeeMapper;
import com.school_management_webapi.repository.FeeRepository;
import com.school_management_webapi.specification.FeeSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FeeServiceImpl implements FeeService {

	private final FeeRepository feeRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public FeeResponse create(UUID userId, FeeCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		Fee entity = FeeMapper.toEntity(request);
		return FeeMapper.toResponse(feeRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public FeeResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return FeeMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<FeeResponse> list(UUID userId, UUID branchId, RecordStatus status, UUID academicYearId, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Fee> page = feeRepository.findAll(
				FeeSpecification.filterBy(allowedBranchIds, branchId, status, academicYearId, search), pageable);
		return PagedResponse.of(page.map(FeeMapper::toResponse));
	}

	@Override
	public FeeResponse update(UUID userId, UUID id, FeeUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Fee entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		FeeMapper.updateEntity(entity, request);
		return FeeMapper.toResponse(feeRepository.saveAndFlush(entity));
	}

	@Override
	public FeeResponse patch(UUID userId, UUID id, FeePatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Fee entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}

		FeeMapper.patchEntity(entity, request);
		return FeeMapper.toResponse(feeRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		feeRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Fee findInTenantOrThrow(UUID id, UUID tenantId) {
		Fee entity = feeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Fee not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}
}

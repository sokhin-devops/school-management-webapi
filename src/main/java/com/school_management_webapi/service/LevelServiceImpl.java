package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.LevelCreateRequest;
import com.school_management_webapi.dto.request.LevelPatchRequest;
import com.school_management_webapi.dto.request.LevelUpdateRequest;
import com.school_management_webapi.dto.response.LevelResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.Level;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.LevelMapper;
import com.school_management_webapi.repository.LevelRepository;
import com.school_management_webapi.specification.LevelSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LevelServiceImpl implements LevelService {

	private final LevelRepository levelRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public LevelResponse create(UUID userId, LevelCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		Level entity = LevelMapper.toEntity(request);
		return LevelMapper.toResponse(levelRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public LevelResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return LevelMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<LevelResponse> list(UUID userId, UUID branchId, RecordStatus status, UUID programId, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Level> page = levelRepository.findAll(
				LevelSpecification.filterBy(allowedBranchIds, branchId, status, programId, search), pageable);
		return PagedResponse.of(page.map(LevelMapper::toResponse));
	}

	@Override
	public LevelResponse update(UUID userId, UUID id, LevelUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Level entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		LevelMapper.updateEntity(entity, request);
		return LevelMapper.toResponse(levelRepository.saveAndFlush(entity));
	}

	@Override
	public LevelResponse patch(UUID userId, UUID id, LevelPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Level entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}

		LevelMapper.patchEntity(entity, request);
		return LevelMapper.toResponse(levelRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		levelRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Level findInTenantOrThrow(UUID id, UUID tenantId) {
		Level entity = levelRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Level not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}
}

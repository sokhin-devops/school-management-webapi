package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.ParentCreateRequest;
import com.school_management_webapi.dto.request.ParentPatchRequest;
import com.school_management_webapi.dto.request.ParentUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.ParentResponse;
import com.school_management_webapi.entity.Parent;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.ParentMapper;
import com.school_management_webapi.repository.ParentRepository;
import com.school_management_webapi.specification.ParentSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentServiceImpl implements ParentService {

	private final ParentRepository parentRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public ParentResponse create(UUID userId, ParentCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		Parent entity = ParentMapper.toEntity(request);
		return ParentMapper.toResponse(parentRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public ParentResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return ParentMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<ParentResponse> list(UUID userId, UUID branchId, RecordStatus status, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Parent> page = parentRepository.findAll(
				ParentSpecification.filterBy(allowedBranchIds, branchId, status, search), pageable);
		return PagedResponse.of(page.map(ParentMapper::toResponse));
	}

	@Override
	public ParentResponse update(UUID userId, UUID id, ParentUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Parent entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		ParentMapper.updateEntity(entity, request);
		return ParentMapper.toResponse(parentRepository.saveAndFlush(entity));
	}

	@Override
	public ParentResponse patch(UUID userId, UUID id, ParentPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Parent entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}

		ParentMapper.patchEntity(entity, request);
		return ParentMapper.toResponse(parentRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		parentRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Parent findInTenantOrThrow(UUID id, UUID tenantId) {
		Parent entity = parentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}
}

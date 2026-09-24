package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.ClassGroupCreateRequest;
import com.school_management_webapi.dto.request.ClassGroupPatchRequest;
import com.school_management_webapi.dto.request.ClassGroupUpdateRequest;
import com.school_management_webapi.dto.response.ClassGroupResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.ClassGroup;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.ClassGroupMapper;
import com.school_management_webapi.repository.ClassGroupRepository;
import com.school_management_webapi.specification.ClassGroupSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassGroupServiceImpl implements ClassGroupService {

	private final ClassGroupRepository classGroupRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public ClassGroupResponse create(UUID userId, ClassGroupCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		if (classGroupRepository.existsByCodeAndBranchId(request.code(), request.branchId())) {
			throw new DuplicateResourceException(
					"A class with code '" + request.code() + "' already exists in this branch");
		}

		ClassGroup entity = ClassGroupMapper.toEntity(request);
		return ClassGroupMapper.toResponse(classGroupRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public ClassGroupResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return ClassGroupMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<ClassGroupResponse> list(UUID userId, UUID branchId, RecordStatus status, UUID academicYearId, UUID levelId, UUID programId, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<ClassGroup> page = classGroupRepository.findAll(
				ClassGroupSpecification.filterBy(allowedBranchIds, branchId, status, academicYearId, levelId, programId, search), pageable);
		return PagedResponse.of(page.map(ClassGroupMapper::toResponse));
	}

	@Override
	public ClassGroupResponse update(UUID userId, UUID id, ClassGroupUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		ClassGroup entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		ensureCodeIsAvailable(id, request.code(), request.branchId());

		ClassGroupMapper.updateEntity(entity, request);
		return ClassGroupMapper.toResponse(classGroupRepository.saveAndFlush(entity));
	}

	@Override
	public ClassGroupResponse patch(UUID userId, UUID id, ClassGroupPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		ClassGroup entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}
		ensureCodeIsAvailable(id, request.code() != null ? request.code() : entity.getCode(),
				request.branchId() != null ? request.branchId() : entity.getBranchId());

		ClassGroupMapper.patchEntity(entity, request);
		return ClassGroupMapper.toResponse(classGroupRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		classGroupRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private ClassGroup findInTenantOrThrow(UUID id, UUID tenantId) {
		ClassGroup entity = classGroupRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("ClassGroup not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}

	private void ensureCodeIsAvailable(UUID id, String code, UUID branchId) {
		classGroupRepository.findByCodeAndBranchId(code, branchId)
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new DuplicateResourceException(
							"A class with code '" + code + "' already exists in this branch");
				});
	}
}

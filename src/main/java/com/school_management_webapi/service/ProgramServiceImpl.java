package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.ProgramCreateRequest;
import com.school_management_webapi.dto.request.ProgramPatchRequest;
import com.school_management_webapi.dto.request.ProgramUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.ProgramResponse;
import com.school_management_webapi.entity.Program;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.ProgramMapper;
import com.school_management_webapi.repository.ProgramRepository;
import com.school_management_webapi.specification.ProgramSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgramServiceImpl implements ProgramService {

	private final ProgramRepository programRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public ProgramResponse create(UUID userId, ProgramCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		if (programRepository.existsByCodeAndBranchId(request.code(), request.branchId())) {
			throw new DuplicateResourceException(
					"A programme with code '" + request.code() + "' already exists in this branch");
		}

		Program entity = ProgramMapper.toEntity(request);
		return ProgramMapper.toResponse(programRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public ProgramResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return ProgramMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<ProgramResponse> list(UUID userId, UUID branchId, RecordStatus status, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Program> page = programRepository.findAll(
				ProgramSpecification.filterBy(allowedBranchIds, branchId, status, search), pageable);
		return PagedResponse.of(page.map(ProgramMapper::toResponse));
	}

	@Override
	public ProgramResponse update(UUID userId, UUID id, ProgramUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Program entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		ensureCodeIsAvailable(id, request.code(), request.branchId());

		ProgramMapper.updateEntity(entity, request);
		return ProgramMapper.toResponse(programRepository.saveAndFlush(entity));
	}

	@Override
	public ProgramResponse patch(UUID userId, UUID id, ProgramPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Program entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}
		ensureCodeIsAvailable(id, request.code() != null ? request.code() : entity.getCode(),
				request.branchId() != null ? request.branchId() : entity.getBranchId());

		ProgramMapper.patchEntity(entity, request);
		return ProgramMapper.toResponse(programRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		programRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Program findInTenantOrThrow(UUID id, UUID tenantId) {
		Program entity = programRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}

	private void ensureCodeIsAvailable(UUID id, String code, UUID branchId) {
		programRepository.findByCodeAndBranchId(code, branchId)
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new DuplicateResourceException(
							"A programme with code '" + code + "' already exists in this branch");
				});
	}
}

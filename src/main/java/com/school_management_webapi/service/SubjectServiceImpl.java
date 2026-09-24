package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.SubjectCreateRequest;
import com.school_management_webapi.dto.request.SubjectPatchRequest;
import com.school_management_webapi.dto.request.SubjectUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.SubjectResponse;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.entity.Subject;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.SubjectMapper;
import com.school_management_webapi.repository.SubjectRepository;
import com.school_management_webapi.specification.SubjectSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectServiceImpl implements SubjectService {

	private final SubjectRepository subjectRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public SubjectResponse create(UUID userId, SubjectCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		if (subjectRepository.existsByCodeAndBranchId(request.code(), request.branchId())) {
			throw new DuplicateResourceException(
					"A subject with code '" + request.code() + "' already exists in this branch");
		}

		Subject entity = SubjectMapper.toEntity(request);
		return SubjectMapper.toResponse(subjectRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public SubjectResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return SubjectMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<SubjectResponse> list(UUID userId, UUID branchId, RecordStatus status, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Subject> page = subjectRepository.findAll(
				SubjectSpecification.filterBy(allowedBranchIds, branchId, status, search), pageable);
		return PagedResponse.of(page.map(SubjectMapper::toResponse));
	}

	@Override
	public SubjectResponse update(UUID userId, UUID id, SubjectUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Subject entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		ensureCodeIsAvailable(id, request.code(), request.branchId());

		SubjectMapper.updateEntity(entity, request);
		return SubjectMapper.toResponse(subjectRepository.saveAndFlush(entity));
	}

	@Override
	public SubjectResponse patch(UUID userId, UUID id, SubjectPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Subject entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}
		ensureCodeIsAvailable(id, request.code() != null ? request.code() : entity.getCode(),
				request.branchId() != null ? request.branchId() : entity.getBranchId());

		SubjectMapper.patchEntity(entity, request);
		return SubjectMapper.toResponse(subjectRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		subjectRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Subject findInTenantOrThrow(UUID id, UUID tenantId) {
		Subject entity = subjectRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}

	private void ensureCodeIsAvailable(UUID id, String code, UUID branchId) {
		subjectRepository.findByCodeAndBranchId(code, branchId)
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new DuplicateResourceException(
							"A subject with code '" + code + "' already exists in this branch");
				});
	}
}

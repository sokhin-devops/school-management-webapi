package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.AssessmentCreateRequest;
import com.school_management_webapi.dto.request.AssessmentPatchRequest;
import com.school_management_webapi.dto.request.AssessmentUpdateRequest;
import com.school_management_webapi.dto.response.AssessmentResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.Assessment;
import com.school_management_webapi.entity.AssessmentType;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.AssessmentMapper;
import com.school_management_webapi.repository.AssessmentRepository;
import com.school_management_webapi.specification.AssessmentSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentServiceImpl implements AssessmentService {

	private final AssessmentRepository assessmentRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public AssessmentResponse create(UUID userId, AssessmentCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		Assessment entity = AssessmentMapper.toEntity(request);
		return AssessmentMapper.toResponse(assessmentRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public AssessmentResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return AssessmentMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<AssessmentResponse> list(UUID userId, UUID branchId, AssessmentType type, UUID classGroupId, UUID subjectId, UUID academicYearId, LocalDate assessedOnFrom, LocalDate assessedOnTo, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Assessment> page = assessmentRepository.findAll(
				AssessmentSpecification.filterBy(allowedBranchIds, branchId, type, classGroupId, subjectId, academicYearId, assessedOnFrom, assessedOnTo, search), pageable);
		return PagedResponse.of(page.map(AssessmentMapper::toResponse));
	}

	@Override
	public AssessmentResponse update(UUID userId, UUID id, AssessmentUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Assessment entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		AssessmentMapper.updateEntity(entity, request);
		return AssessmentMapper.toResponse(assessmentRepository.saveAndFlush(entity));
	}

	@Override
	public AssessmentResponse patch(UUID userId, UUID id, AssessmentPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Assessment entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}

		AssessmentMapper.patchEntity(entity, request);
		return AssessmentMapper.toResponse(assessmentRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		assessmentRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Assessment findInTenantOrThrow(UUID id, UUID tenantId) {
		Assessment entity = assessmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}
}

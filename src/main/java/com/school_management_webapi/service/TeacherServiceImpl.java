package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.TeacherCreateRequest;
import com.school_management_webapi.dto.request.TeacherPatchRequest;
import com.school_management_webapi.dto.request.TeacherUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.TeacherResponse;
import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.entity.Teacher;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.TeacherMapper;
import com.school_management_webapi.repository.TeacherRepository;
import com.school_management_webapi.specification.TeacherSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherServiceImpl implements TeacherService {

	private final TeacherRepository teacherRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public TeacherResponse create(UUID userId, TeacherCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		if (teacherRepository.existsByEmployeeNumberAndBranchId(request.employeeNumber(), request.branchId())) {
			throw new DuplicateResourceException(
					"A teacher with employeeNumber '" + request.employeeNumber() + "' already exists in this branch");
		}

		Teacher entity = TeacherMapper.toEntity(request);
		return TeacherMapper.toResponse(teacherRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public TeacherResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return TeacherMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<TeacherResponse> list(UUID userId, UUID branchId, RecordStatus status, Gender gender, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Teacher> page = teacherRepository.findAll(
				TeacherSpecification.filterBy(allowedBranchIds, branchId, status, gender, search), pageable);
		return PagedResponse.of(page.map(TeacherMapper::toResponse));
	}

	@Override
	public TeacherResponse update(UUID userId, UUID id, TeacherUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Teacher entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		ensureEmployeeNumberIsAvailable(id, request.employeeNumber(), request.branchId());

		TeacherMapper.updateEntity(entity, request);
		return TeacherMapper.toResponse(teacherRepository.saveAndFlush(entity));
	}

	@Override
	public TeacherResponse patch(UUID userId, UUID id, TeacherPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Teacher entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}
		ensureEmployeeNumberIsAvailable(id, request.employeeNumber() != null ? request.employeeNumber() : entity.getEmployeeNumber(),
				request.branchId() != null ? request.branchId() : entity.getBranchId());

		TeacherMapper.patchEntity(entity, request);
		return TeacherMapper.toResponse(teacherRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		teacherRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Teacher findInTenantOrThrow(UUID id, UUID tenantId) {
		Teacher entity = teacherRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}

	private void ensureEmployeeNumberIsAvailable(UUID id, String employeeNumber, UUID branchId) {
		teacherRepository.findByEmployeeNumberAndBranchId(employeeNumber, branchId)
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new DuplicateResourceException(
							"A teacher with employeeNumber '" + employeeNumber + "' already exists in this branch");
				});
	}
}

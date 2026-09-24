package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.AttendanceRecordCreateRequest;
import com.school_management_webapi.dto.request.AttendanceRecordPatchRequest;
import com.school_management_webapi.dto.request.AttendanceRecordUpdateRequest;
import com.school_management_webapi.dto.response.AttendanceRecordResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.AttendanceRecord;
import com.school_management_webapi.entity.AttendanceStatus;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.AttendanceRecordMapper;
import com.school_management_webapi.repository.AttendanceRecordRepository;
import com.school_management_webapi.specification.AttendanceRecordSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

	private final AttendanceRecordRepository attendanceRecordRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public AttendanceRecordResponse create(UUID userId, AttendanceRecordCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		AttendanceRecord entity = AttendanceRecordMapper.toEntity(request);
		return AttendanceRecordMapper.toResponse(attendanceRecordRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public AttendanceRecordResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return AttendanceRecordMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<AttendanceRecordResponse> list(UUID userId, UUID branchId, AttendanceStatus status, UUID classGroupId, UUID studentId, LocalDate attendanceDateFrom, LocalDate attendanceDateTo, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<AttendanceRecord> page = attendanceRecordRepository.findAll(
				AttendanceRecordSpecification.filterBy(allowedBranchIds, branchId, status, classGroupId, studentId, attendanceDateFrom, attendanceDateTo, search), pageable);
		return PagedResponse.of(page.map(AttendanceRecordMapper::toResponse));
	}

	@Override
	public AttendanceRecordResponse update(UUID userId, UUID id, AttendanceRecordUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		AttendanceRecord entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		AttendanceRecordMapper.updateEntity(entity, request);
		return AttendanceRecordMapper.toResponse(attendanceRecordRepository.saveAndFlush(entity));
	}

	@Override
	public AttendanceRecordResponse patch(UUID userId, UUID id, AttendanceRecordPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		AttendanceRecord entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}

		AttendanceRecordMapper.patchEntity(entity, request);
		return AttendanceRecordMapper.toResponse(attendanceRecordRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		attendanceRecordRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private AttendanceRecord findInTenantOrThrow(UUID id, UUID tenantId) {
		AttendanceRecord entity = attendanceRecordRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("AttendanceRecord not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}
}

package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.RoomCreateRequest;
import com.school_management_webapi.dto.request.RoomPatchRequest;
import com.school_management_webapi.dto.request.RoomUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.RoomResponse;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.entity.Room;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.RoomMapper;
import com.school_management_webapi.repository.RoomRepository;
import com.school_management_webapi.specification.RoomSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

	private final RoomRepository roomRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public RoomResponse create(UUID userId, RoomCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		if (roomRepository.existsByCodeAndBranchId(request.code(), request.branchId())) {
			throw new DuplicateResourceException(
					"A room with code '" + request.code() + "' already exists in this branch");
		}

		Room entity = RoomMapper.toEntity(request);
		return RoomMapper.toResponse(roomRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public RoomResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return RoomMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<RoomResponse> list(UUID userId, UUID branchId, RecordStatus status, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Room> page = roomRepository.findAll(
				RoomSpecification.filterBy(allowedBranchIds, branchId, status, search), pageable);
		return PagedResponse.of(page.map(RoomMapper::toResponse));
	}

	@Override
	public RoomResponse update(UUID userId, UUID id, RoomUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Room entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		ensureCodeIsAvailable(id, request.code(), request.branchId());

		RoomMapper.updateEntity(entity, request);
		return RoomMapper.toResponse(roomRepository.saveAndFlush(entity));
	}

	@Override
	public RoomResponse patch(UUID userId, UUID id, RoomPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Room entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}
		ensureCodeIsAvailable(id, request.code() != null ? request.code() : entity.getCode(),
				request.branchId() != null ? request.branchId() : entity.getBranchId());

		RoomMapper.patchEntity(entity, request);
		return RoomMapper.toResponse(roomRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		roomRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Room findInTenantOrThrow(UUID id, UUID tenantId) {
		Room entity = roomRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}

	private void ensureCodeIsAvailable(UUID id, String code, UUID branchId) {
		roomRepository.findByCodeAndBranchId(code, branchId)
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new DuplicateResourceException(
							"A room with code '" + code + "' already exists in this branch");
				});
	}
}

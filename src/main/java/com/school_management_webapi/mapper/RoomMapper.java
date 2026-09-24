package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.RoomCreateRequest;
import com.school_management_webapi.dto.request.RoomPatchRequest;
import com.school_management_webapi.dto.request.RoomUpdateRequest;
import com.school_management_webapi.dto.response.RoomResponse;
import com.school_management_webapi.entity.Room;

public final class RoomMapper {

	private RoomMapper() {
	}

	public static Room toEntity(RoomCreateRequest request) {
		return Room.builder()
				.branchId(request.branchId())
				.name(request.name())
				.code(request.code())
				.building(request.building())
				.kind(request.kind())
				.capacity(request.capacity())
				.status(request.status())
				.build();
	}

	public static void updateEntity(Room entity, RoomUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setName(request.name());
		entity.setCode(request.code());
		entity.setBuilding(request.building());
		entity.setKind(request.kind());
		entity.setCapacity(request.capacity());
		entity.setStatus(request.status());
	}

	public static void patchEntity(Room entity, RoomPatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.name() != null) {
			entity.setName(request.name());
		}

		if (request.code() != null) {
			entity.setCode(request.code());
		}

		if (request.building() != null) {
			entity.setBuilding(request.building());
		}

		if (request.kind() != null) {
			entity.setKind(request.kind());
		}

		if (request.capacity() != null) {
			entity.setCapacity(request.capacity());
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}
	}

	public static RoomResponse toResponse(Room entity) {
		return new RoomResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getName(),
				entity.getCode(),
				entity.getBuilding(),
				entity.getKind(),
				entity.getCapacity(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.LevelCreateRequest;
import com.school_management_webapi.dto.request.LevelPatchRequest;
import com.school_management_webapi.dto.request.LevelUpdateRequest;
import com.school_management_webapi.dto.response.LevelResponse;
import com.school_management_webapi.entity.Level;

public final class LevelMapper {

	private LevelMapper() {
	}

	public static Level toEntity(LevelCreateRequest request) {
		return Level.builder()
				.branchId(request.branchId())
				.programId(request.programId())
				.name(request.name())
				.displayLabel(request.displayLabel())
				.order(request.order())
				.status(request.status())
				.build();
	}

	public static void updateEntity(Level entity, LevelUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setProgramId(request.programId());
		entity.setName(request.name());
		entity.setDisplayLabel(request.displayLabel());
		entity.setOrder(request.order());
		entity.setStatus(request.status());
	}

	public static void patchEntity(Level entity, LevelPatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.programId() != null) {
			entity.setProgramId(request.programId());
		}

		if (request.name() != null) {
			entity.setName(request.name());
		}

		if (request.displayLabel() != null) {
			entity.setDisplayLabel(request.displayLabel());
		}

		if (request.order() != null) {
			entity.setOrder(request.order());
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}
	}

	public static LevelResponse toResponse(Level entity) {
		return new LevelResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getProgramId(),
				entity.getName(),
				entity.getDisplayLabel(),
				entity.getOrder(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

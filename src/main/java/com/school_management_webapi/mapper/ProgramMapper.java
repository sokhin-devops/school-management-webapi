package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.ProgramCreateRequest;
import com.school_management_webapi.dto.request.ProgramPatchRequest;
import com.school_management_webapi.dto.request.ProgramUpdateRequest;
import com.school_management_webapi.dto.response.ProgramResponse;
import com.school_management_webapi.entity.Program;

public final class ProgramMapper {

	private ProgramMapper() {
	}

	public static Program toEntity(ProgramCreateRequest request) {
		return Program.builder()
				.branchId(request.branchId())
				.name(request.name())
				.code(request.code())
				.description(request.description())
				.status(request.status())
				.build();
	}

	public static void updateEntity(Program entity, ProgramUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setName(request.name());
		entity.setCode(request.code());
		entity.setDescription(request.description());
		entity.setStatus(request.status());
	}

	public static void patchEntity(Program entity, ProgramPatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.name() != null) {
			entity.setName(request.name());
		}

		if (request.code() != null) {
			entity.setCode(request.code());
		}

		if (request.description() != null) {
			entity.setDescription(request.description());
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}
	}

	public static ProgramResponse toResponse(Program entity) {
		return new ProgramResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getName(),
				entity.getCode(),
				entity.getDescription(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

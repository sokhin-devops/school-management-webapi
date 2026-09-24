package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.SubjectCreateRequest;
import com.school_management_webapi.dto.request.SubjectPatchRequest;
import com.school_management_webapi.dto.request.SubjectUpdateRequest;
import com.school_management_webapi.dto.response.SubjectResponse;
import com.school_management_webapi.entity.Subject;

public final class SubjectMapper {

	private SubjectMapper() {
	}

	public static Subject toEntity(SubjectCreateRequest request) {
		return Subject.builder()
				.branchId(request.branchId())
				.name(request.name())
				.code(request.code())
				.description(request.description())
				.status(request.status())
				.build();
	}

	public static void updateEntity(Subject entity, SubjectUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setName(request.name());
		entity.setCode(request.code());
		entity.setDescription(request.description());
		entity.setStatus(request.status());
	}

	public static void patchEntity(Subject entity, SubjectPatchRequest request) {
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

	public static SubjectResponse toResponse(Subject entity) {
		return new SubjectResponse(
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

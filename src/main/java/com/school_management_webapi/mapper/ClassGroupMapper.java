package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.ClassGroupCreateRequest;
import com.school_management_webapi.dto.request.ClassGroupPatchRequest;
import com.school_management_webapi.dto.request.ClassGroupUpdateRequest;
import com.school_management_webapi.dto.response.ClassGroupResponse;
import com.school_management_webapi.entity.ClassGroup;

public final class ClassGroupMapper {

	private ClassGroupMapper() {
	}

	public static ClassGroup toEntity(ClassGroupCreateRequest request) {
		return ClassGroup.builder()
				.branchId(request.branchId())
				.academicYearId(request.academicYearId())
				.programId(request.programId())
				.levelId(request.levelId())
				.parentClassId(request.parentClassId())
				.homeroomTeacherId(request.homeroomTeacherId())
				.name(request.name())
				.code(request.code())
				.capacity(request.capacity())
				.status(request.status())
				.build();
	}

	public static void updateEntity(ClassGroup entity, ClassGroupUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setAcademicYearId(request.academicYearId());
		entity.setProgramId(request.programId());
		entity.setLevelId(request.levelId());
		entity.setParentClassId(request.parentClassId());
		entity.setHomeroomTeacherId(request.homeroomTeacherId());
		entity.setName(request.name());
		entity.setCode(request.code());
		entity.setCapacity(request.capacity());
		entity.setStatus(request.status());
	}

	public static void patchEntity(ClassGroup entity, ClassGroupPatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.academicYearId() != null) {
			entity.setAcademicYearId(request.academicYearId());
		}

		if (request.programId() != null) {
			entity.setProgramId(request.programId());
		}

		if (request.levelId() != null) {
			entity.setLevelId(request.levelId());
		}

		if (request.parentClassId() != null) {
			entity.setParentClassId(request.parentClassId());
		}

		if (request.homeroomTeacherId() != null) {
			entity.setHomeroomTeacherId(request.homeroomTeacherId());
		}

		if (request.name() != null) {
			entity.setName(request.name());
		}

		if (request.code() != null) {
			entity.setCode(request.code());
		}

		if (request.capacity() != null) {
			entity.setCapacity(request.capacity());
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}
	}

	public static ClassGroupResponse toResponse(ClassGroup entity) {
		return new ClassGroupResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getAcademicYearId(),
				entity.getProgramId(),
				entity.getLevelId(),
				entity.getParentClassId(),
				entity.getHomeroomTeacherId(),
				entity.getName(),
				entity.getCode(),
				entity.getCapacity(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

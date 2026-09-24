package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.FeeCreateRequest;
import com.school_management_webapi.dto.request.FeePatchRequest;
import com.school_management_webapi.dto.request.FeeUpdateRequest;
import com.school_management_webapi.dto.response.FeeResponse;
import com.school_management_webapi.entity.Fee;

public final class FeeMapper {

	private FeeMapper() {
	}

	public static Fee toEntity(FeeCreateRequest request) {
		return Fee.builder()
				.branchId(request.branchId())
				.academicYearId(request.academicYearId())
				.name(request.name())
				.category(request.category())
				.amount(request.amount())
				.description(request.description())
				.status(request.status())
				.build();
	}

	public static void updateEntity(Fee entity, FeeUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setAcademicYearId(request.academicYearId());
		entity.setName(request.name());
		entity.setCategory(request.category());
		entity.setAmount(request.amount());
		entity.setDescription(request.description());
		entity.setStatus(request.status());
	}

	public static void patchEntity(Fee entity, FeePatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.academicYearId() != null) {
			entity.setAcademicYearId(request.academicYearId());
		}

		if (request.name() != null) {
			entity.setName(request.name());
		}

		if (request.category() != null) {
			entity.setCategory(request.category());
		}

		if (request.amount() != null) {
			entity.setAmount(request.amount());
		}

		if (request.description() != null) {
			entity.setDescription(request.description());
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}
	}

	public static FeeResponse toResponse(Fee entity) {
		return new FeeResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getAcademicYearId(),
				entity.getName(),
				entity.getCategory(),
				entity.getAmount(),
				entity.getDescription(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

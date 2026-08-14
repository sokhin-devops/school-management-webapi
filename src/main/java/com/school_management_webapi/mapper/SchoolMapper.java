package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.response.SchoolResponse;
import com.school_management_webapi.entity.School;

public final class SchoolMapper {

	private SchoolMapper() {
	}

	public static SchoolResponse toResponse(School school) {
		return new SchoolResponse(
				school.getId(),
				school.getTenant().getId(),
				school.getName(),
				school.getType(),
				school.getEmail(),
				school.getPhone(),
				school.getAddress(),
				school.getStatus(),
				school.getCreatedAt(),
				school.getUpdatedAt());
	}
}

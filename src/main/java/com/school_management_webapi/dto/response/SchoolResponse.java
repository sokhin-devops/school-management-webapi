package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.SchoolStatus;
import com.school_management_webapi.entity.SchoolType;

public record SchoolResponse(
		UUID id,
		UUID tenantId,
		String name,
		SchoolType type,
		String email,
		String phone,
		String address,
		String shortName,
		String website,
		String currency,
		SchoolStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

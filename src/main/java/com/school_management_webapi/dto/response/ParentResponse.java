package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

public record ParentResponse(
		UUID id,
		UUID branchId,
		String firstName,
		String lastName,
		String relationship,
		String email,
		String phone,
		List<UUID> studentIds,
		RecordStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

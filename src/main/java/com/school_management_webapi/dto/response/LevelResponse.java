package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

public record LevelResponse(
		UUID id,
		UUID branchId,
		UUID programId,
		String name,
		String displayLabel,
		Integer order,
		RecordStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

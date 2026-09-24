package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

public record FeeResponse(
		UUID id,
		UUID branchId,
		UUID academicYearId,
		String name,
		String category,
		BigDecimal amount,
		String description,
		RecordStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

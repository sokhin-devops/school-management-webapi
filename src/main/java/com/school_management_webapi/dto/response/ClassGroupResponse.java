package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

public record ClassGroupResponse(
		UUID id,
		UUID branchId,
		UUID academicYearId,
		UUID programId,
		UUID levelId,
		UUID parentClassId,
		UUID homeroomTeacherId,
		String name,
		String code,
		Integer capacity,
		RecordStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

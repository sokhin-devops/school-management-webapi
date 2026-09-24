package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

public record RoomResponse(
		UUID id,
		UUID branchId,
		String name,
		String code,
		String building,
		String kind,
		Integer capacity,
		RecordStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

package com.school_management_webapi.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.AttendanceStatus;

public record AttendanceRecordResponse(
		UUID id,
		UUID branchId,
		UUID classGroupId,
		UUID studentId,
		LocalDate attendanceDate,
		String session,
		AttendanceStatus status,
		String note,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

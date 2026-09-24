package com.school_management_webapi.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.AttendanceStatus;

import jakarta.validation.constraints.Size;

/** A partial change. Every field is optional; the ones left out keep their current value. */
public record AttendanceRecordPatchRequest(
		UUID branchId,

		UUID classGroupId,

		UUID studentId,

		LocalDate attendanceDate,

		@Size(max = 50, message = "session must be 50 characters or fewer") String session,

		AttendanceStatus status,

		@Size(max = 255, message = "note must be 255 characters or fewer") String note) {
}

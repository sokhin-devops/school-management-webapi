package com.school_management_webapi.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A full replacement: every field is written, so every required one must be present. */
public record AttendanceRecordUpdateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		@NotNull(message = "classGroupId is required") UUID classGroupId,

		@NotNull(message = "studentId is required") UUID studentId,

		@NotNull(message = "attendanceDate is required") LocalDate attendanceDate,

		@Size(max = 50, message = "session must be 50 characters or fewer") String session,

		@NotNull(message = "status is required") AttendanceStatus status,

		@Size(max = 255, message = "note must be 255 characters or fewer") String note) {
}

package com.school_management_webapi.dto.request;

import java.util.UUID;

import com.school_management_webapi.entity.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** One student's mark in a register. */
public record AttendanceEntryRequest(
		@NotNull(message = "studentId is required") UUID studentId,

		@NotNull(message = "status is required") AttendanceStatus status,

		@Size(max = 255, message = "note must be 255 characters or fewer") String note) {
}

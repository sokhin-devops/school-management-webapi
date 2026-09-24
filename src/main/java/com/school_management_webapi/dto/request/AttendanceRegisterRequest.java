package com.school_management_webapi.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A whole class marked for one sitting.
 *
 * Attendance is taken a class at a time, not a student at a time, so this is
 * the shape the module is actually used through; the per-record endpoints exist
 * for corrections afterwards.
 *
 * Saving replaces the sitting: a student left out is a student with no mark,
 * which is what removing a row from the register in front of you should mean.
 */
public record AttendanceRegisterRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		@NotNull(message = "classGroupId is required") UUID classGroupId,

		@NotNull(message = "attendanceDate is required") LocalDate attendanceDate,

		/** Blank when the class is taken once a day. */
		@Size(max = 50, message = "session must be 50 characters or fewer") String session,

		@NotNull(message = "entries is required") @Valid List<AttendanceEntryRequest> entries) {
}

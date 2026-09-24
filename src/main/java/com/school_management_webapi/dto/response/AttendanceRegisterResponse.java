package com.school_management_webapi.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A marked register, with the tallies a class teacher reads first so the client
 * does not have to count the rows itself.
 */
public record AttendanceRegisterResponse(
		UUID branchId,
		UUID classGroupId,
		LocalDate attendanceDate,
		String session,
		int present,
		int absent,
		int late,
		int excused,
		List<AttendanceRecordResponse> entries) {
}

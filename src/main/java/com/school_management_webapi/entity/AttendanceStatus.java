package com.school_management_webapi.entity;

/**
 * One student's attendance for one session. 30-attendance.md: the wording
 * is configurable in the UI, but these four states are what is stored.
 */
public enum AttendanceStatus {
	PRESENT,
	ABSENT,
	LATE,
	EXCUSED
}

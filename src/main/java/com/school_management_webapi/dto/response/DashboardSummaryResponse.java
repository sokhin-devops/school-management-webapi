package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Everything the dashboard shows, in one call.
 *
 * Assembled server-side rather than left to the client to stitch from a dozen
 * list endpoints: the numbers are aggregates, and counting them in the database
 * is both faster and the only way they can be consistent with each other.
 */
public record DashboardSummaryResponse(
		long students,
		long teachers,
		long parents,
		long classes,
		AttendanceSummary attendanceToday,
		BigDecimal collectedThisMonth,
		BigDecimal spentThisMonth,
		List<AttendancePoint> attendanceTrend,
		List<CollectionPoint> collectionTrend) {

	/** Null percent means nothing was marked, which is not the same as nobody came. */
	public record AttendanceSummary(long present, long marked, Double percent) {
	}

	public record AttendancePoint(LocalDate date, long present, long marked, Double percent) {
	}

	public record CollectionPoint(String month, BigDecimal collected, BigDecimal spent) {
	}
}

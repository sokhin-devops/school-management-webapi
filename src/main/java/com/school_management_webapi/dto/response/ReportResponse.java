package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * The four reports of 50-reports.md, in one file.
 *
 * The framework is shared and only the templates differ, so the rows live
 * together rather than in four near-identical files that would drift apart.
 */
public final class ReportResponse {

	private ReportResponse() {
	}

	/** One class, and how full it is. */
	public record EnrolmentRow(
			String className,
			long enrolled,
			Integer capacity,
			long active,
			long inactive) {
	}

	/** One class, and how its registers have gone over the period asked for. */
	public record AttendanceRow(
			String className,
			long sessions,
			long present,
			long absent,
			long late,
			long excused) {
	}

	/** One subject, and how its marked assessments came out. */
	public record AcademicRow(
			String subject,
			long assessments,
			BigDecimal average,
			BigDecimal highest,
			BigDecimal lowest,
			BigDecimal passRate) {
	}

	/** One fee category: what it could bill, and what came in against it. */
	public record FinancialRow(
			String category,
			BigDecimal invoiced,
			BigDecimal collected) {
	}

	/** One month of money in and money out. */
	public record LedgerRow(
			String month,
			BigDecimal collected,
			BigDecimal expenses) {
	}

	public record EnrolmentReport(List<EnrolmentRow> rows) {
	}

	public record AttendanceReport(List<AttendanceRow> rows) {
	}

	public record AcademicReport(List<AcademicRow> rows) {
	}

	public record FinancialReport(List<FinancialRow> categories, List<LedgerRow> ledger) {
	}
}

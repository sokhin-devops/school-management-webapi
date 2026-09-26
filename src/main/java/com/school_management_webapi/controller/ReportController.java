package com.school_management_webapi.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.response.ReportResponse.AcademicReport;
import com.school_management_webapi.dto.response.ReportResponse.AttendanceReport;
import com.school_management_webapi.dto.response.ReportResponse.EnrolmentReport;
import com.school_management_webapi.dto.response.ReportResponse.FinancialReport;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 50-reports.md. Every report takes the same optional branch, so the whole
 * section can be read for one branch or for the tenant.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
public class ReportController {

	private final ReportService reportService;

	@GetMapping("/enrolment")
	@Operation(summary = "Students per class, against the capacity of each")
	public ResponseEntity<EnrolmentReport> enrolment(@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId) {
		return ResponseEntity.ok(reportService.enrolment(principal.getUser().getId(), branchId));
	}

	@GetMapping("/attendance")
	@Operation(summary = "Attendance per class over a period, defaulting to the last month")
	public ResponseEntity<AttendanceReport> attendance(@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return ResponseEntity.ok(reportService.attendance(principal.getUser().getId(), branchId, from, to));
	}

	@GetMapping("/academic")
	@Operation(summary = "Assessment results per subject, taken across marked papers only")
	public ResponseEntity<AcademicReport> academic(@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId) {
		return ResponseEntity.ok(reportService.academic(principal.getUser().getId(), branchId));
	}

	@GetMapping("/financial")
	@Operation(summary = "Collection by fee category, with a six-month ledger of money in and out")
	public ResponseEntity<FinancialReport> financial(@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return ResponseEntity.ok(reportService.financial(principal.getUser().getId(), branchId, from, to));
	}
}

package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.dto.response.ReportResponse.AcademicReport;
import com.school_management_webapi.dto.response.ReportResponse.AttendanceReport;
import com.school_management_webapi.dto.response.ReportResponse.EnrolmentReport;
import com.school_management_webapi.dto.response.ReportResponse.FinancialReport;

public interface ReportService {

	EnrolmentReport enrolment(UUID userId, UUID branchId);

	AttendanceReport attendance(UUID userId, UUID branchId, LocalDate from, LocalDate to);

	AcademicReport academic(UUID userId, UUID branchId);

	FinancialReport financial(UUID userId, UUID branchId, LocalDate from, LocalDate to);
}

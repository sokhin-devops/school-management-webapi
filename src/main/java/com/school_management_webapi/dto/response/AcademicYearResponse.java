package com.school_management_webapi.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AcademicYearResponse(
		UUID id,
		UUID schoolId,
		String name,
		LocalDate startDate,
		LocalDate endDate,
		boolean current,
		List<Term> terms,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public record Term(String name, LocalDate startDate, LocalDate endDate) {
	}
}

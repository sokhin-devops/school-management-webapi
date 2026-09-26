package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.response.AcademicYearResponse;
import com.school_management_webapi.entity.AcademicYear;

public final class AcademicYearMapper {

	private AcademicYearMapper() {
	}

	public static AcademicYearResponse toResponse(AcademicYear academicYear) {
		return new AcademicYearResponse(
				academicYear.getId(),
				academicYear.getSchool().getId(),
				academicYear.getName(),
				academicYear.getStartDate(),
				academicYear.getEndDate(),
				academicYear.isCurrent(),
				academicYear.getTerms().stream()
						.map(term -> new AcademicYearResponse.Term(term.getName(), term.getStartDate(), term.getEndDate()))
						.toList(),
				academicYear.getCreatedAt(),
				academicYear.getUpdatedAt());
	}
}

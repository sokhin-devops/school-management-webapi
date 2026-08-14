package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.dto.request.AcademicYearRequest;
import com.school_management_webapi.dto.response.AcademicYearResponse;

public interface AcademicYearService {

	AcademicYearResponse create(UUID userId, AcademicYearRequest request);

	List<AcademicYearResponse> list(UUID userId, UUID schoolId);

	AcademicYearResponse getById(UUID userId, UUID academicYearId);

	AcademicYearResponse update(UUID userId, UUID academicYearId, AcademicYearRequest request);

	void delete(UUID userId, UUID academicYearId);
}

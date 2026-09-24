package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.AssessmentCreateRequest;
import com.school_management_webapi.dto.request.AssessmentPatchRequest;
import com.school_management_webapi.dto.request.AssessmentUpdateRequest;
import com.school_management_webapi.dto.response.AssessmentResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.AssessmentType;

public interface AssessmentService {

	AssessmentResponse create(UUID userId, AssessmentCreateRequest request);

	AssessmentResponse getById(UUID userId, UUID id);

	PagedResponse<AssessmentResponse> list(UUID userId, UUID branchId, AssessmentType type, UUID classGroupId, UUID subjectId, UUID academicYearId, LocalDate assessedOnFrom, LocalDate assessedOnTo, String search, Pageable pageable);

	AssessmentResponse update(UUID userId, UUID id, AssessmentUpdateRequest request);

	AssessmentResponse patch(UUID userId, UUID id, AssessmentPatchRequest request);

	void delete(UUID userId, UUID id);
}

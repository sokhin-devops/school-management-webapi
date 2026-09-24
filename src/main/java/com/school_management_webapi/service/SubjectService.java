package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.SubjectCreateRequest;
import com.school_management_webapi.dto.request.SubjectPatchRequest;
import com.school_management_webapi.dto.request.SubjectUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.SubjectResponse;
import com.school_management_webapi.entity.RecordStatus;

public interface SubjectService {

	SubjectResponse create(UUID userId, SubjectCreateRequest request);

	SubjectResponse getById(UUID userId, UUID id);

	PagedResponse<SubjectResponse> list(UUID userId, UUID branchId, RecordStatus status, String search, Pageable pageable);

	SubjectResponse update(UUID userId, UUID id, SubjectUpdateRequest request);

	SubjectResponse patch(UUID userId, UUID id, SubjectPatchRequest request);

	void delete(UUID userId, UUID id);
}

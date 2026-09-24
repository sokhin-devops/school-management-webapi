package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.ProgramCreateRequest;
import com.school_management_webapi.dto.request.ProgramPatchRequest;
import com.school_management_webapi.dto.request.ProgramUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.ProgramResponse;
import com.school_management_webapi.entity.RecordStatus;

public interface ProgramService {

	ProgramResponse create(UUID userId, ProgramCreateRequest request);

	ProgramResponse getById(UUID userId, UUID id);

	PagedResponse<ProgramResponse> list(UUID userId, UUID branchId, RecordStatus status, String search, Pageable pageable);

	ProgramResponse update(UUID userId, UUID id, ProgramUpdateRequest request);

	ProgramResponse patch(UUID userId, UUID id, ProgramPatchRequest request);

	void delete(UUID userId, UUID id);
}

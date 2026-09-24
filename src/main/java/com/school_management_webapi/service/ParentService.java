package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.ParentCreateRequest;
import com.school_management_webapi.dto.request.ParentPatchRequest;
import com.school_management_webapi.dto.request.ParentUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.ParentResponse;
import com.school_management_webapi.entity.RecordStatus;

public interface ParentService {

	ParentResponse create(UUID userId, ParentCreateRequest request);

	ParentResponse getById(UUID userId, UUID id);

	PagedResponse<ParentResponse> list(UUID userId, UUID branchId, RecordStatus status, String search, Pageable pageable);

	ParentResponse update(UUID userId, UUID id, ParentUpdateRequest request);

	ParentResponse patch(UUID userId, UUID id, ParentPatchRequest request);

	void delete(UUID userId, UUID id);
}

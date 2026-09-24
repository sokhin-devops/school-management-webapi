package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.LevelCreateRequest;
import com.school_management_webapi.dto.request.LevelPatchRequest;
import com.school_management_webapi.dto.request.LevelUpdateRequest;
import com.school_management_webapi.dto.response.LevelResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.RecordStatus;

public interface LevelService {

	LevelResponse create(UUID userId, LevelCreateRequest request);

	LevelResponse getById(UUID userId, UUID id);

	PagedResponse<LevelResponse> list(UUID userId, UUID branchId, RecordStatus status, UUID programId, String search, Pageable pageable);

	LevelResponse update(UUID userId, UUID id, LevelUpdateRequest request);

	LevelResponse patch(UUID userId, UUID id, LevelPatchRequest request);

	void delete(UUID userId, UUID id);
}

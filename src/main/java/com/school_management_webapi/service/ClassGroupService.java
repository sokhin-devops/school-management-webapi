package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.ClassGroupCreateRequest;
import com.school_management_webapi.dto.request.ClassGroupPatchRequest;
import com.school_management_webapi.dto.request.ClassGroupUpdateRequest;
import com.school_management_webapi.dto.response.ClassGroupResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.RecordStatus;

public interface ClassGroupService {

	ClassGroupResponse create(UUID userId, ClassGroupCreateRequest request);

	ClassGroupResponse getById(UUID userId, UUID id);

	PagedResponse<ClassGroupResponse> list(UUID userId, UUID branchId, RecordStatus status, UUID academicYearId, UUID levelId, UUID programId, String search, Pageable pageable);

	ClassGroupResponse update(UUID userId, UUID id, ClassGroupUpdateRequest request);

	ClassGroupResponse patch(UUID userId, UUID id, ClassGroupPatchRequest request);

	void delete(UUID userId, UUID id);
}

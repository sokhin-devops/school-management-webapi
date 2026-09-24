package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.FeeCreateRequest;
import com.school_management_webapi.dto.request.FeePatchRequest;
import com.school_management_webapi.dto.request.FeeUpdateRequest;
import com.school_management_webapi.dto.response.FeeResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.RecordStatus;

public interface FeeService {

	FeeResponse create(UUID userId, FeeCreateRequest request);

	FeeResponse getById(UUID userId, UUID id);

	PagedResponse<FeeResponse> list(UUID userId, UUID branchId, RecordStatus status, UUID academicYearId, String search, Pageable pageable);

	FeeResponse update(UUID userId, UUID id, FeeUpdateRequest request);

	FeeResponse patch(UUID userId, UUID id, FeePatchRequest request);

	void delete(UUID userId, UUID id);
}

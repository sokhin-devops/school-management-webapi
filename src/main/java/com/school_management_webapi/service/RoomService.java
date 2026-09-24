package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.RoomCreateRequest;
import com.school_management_webapi.dto.request.RoomPatchRequest;
import com.school_management_webapi.dto.request.RoomUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.RoomResponse;
import com.school_management_webapi.entity.RecordStatus;

public interface RoomService {

	RoomResponse create(UUID userId, RoomCreateRequest request);

	RoomResponse getById(UUID userId, UUID id);

	PagedResponse<RoomResponse> list(UUID userId, UUID branchId, RecordStatus status, String search, Pageable pageable);

	RoomResponse update(UUID userId, UUID id, RoomUpdateRequest request);

	RoomResponse patch(UUID userId, UUID id, RoomPatchRequest request);

	void delete(UUID userId, UUID id);
}

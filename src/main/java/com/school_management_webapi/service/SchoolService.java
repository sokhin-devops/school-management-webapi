package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.dto.request.SchoolRequest;
import com.school_management_webapi.dto.response.SchoolResponse;

public interface SchoolService {

	SchoolResponse create(UUID userId, SchoolRequest request);

	List<SchoolResponse> list(UUID userId);

	SchoolResponse getById(UUID userId, UUID schoolId);

	SchoolResponse update(UUID userId, UUID schoolId, SchoolRequest request);

	void delete(UUID userId, UUID schoolId);
}

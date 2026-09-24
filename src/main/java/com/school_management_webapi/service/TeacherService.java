package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.TeacherCreateRequest;
import com.school_management_webapi.dto.request.TeacherPatchRequest;
import com.school_management_webapi.dto.request.TeacherUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.TeacherResponse;
import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.RecordStatus;

public interface TeacherService {

	TeacherResponse create(UUID userId, TeacherCreateRequest request);

	TeacherResponse getById(UUID userId, UUID id);

	PagedResponse<TeacherResponse> list(UUID userId, UUID branchId, RecordStatus status, Gender gender, String search, Pageable pageable);

	TeacherResponse update(UUID userId, UUID id, TeacherUpdateRequest request);

	TeacherResponse patch(UUID userId, UUID id, TeacherPatchRequest request);

	void delete(UUID userId, UUID id);
}

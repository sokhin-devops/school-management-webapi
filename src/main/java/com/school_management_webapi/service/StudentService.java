package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.StudentCreateRequest;
import com.school_management_webapi.dto.request.StudentPatchRequest;
import com.school_management_webapi.dto.request.StudentUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.StudentResponse;
import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.StudentStatus;

public interface StudentService {

	StudentResponse create(StudentCreateRequest request);

	StudentResponse getById(UUID id);

	PagedResponse<StudentResponse> list(UUID schoolId, StudentStatus status, Gender gender, String search,
			Pageable pageable);

	StudentResponse update(UUID id, StudentUpdateRequest request);

	StudentResponse patch(UUID id, StudentPatchRequest request);

	void delete(UUID id);
}

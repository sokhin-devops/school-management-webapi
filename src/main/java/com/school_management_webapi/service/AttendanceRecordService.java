package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.AttendanceRecordCreateRequest;
import com.school_management_webapi.dto.request.AttendanceRecordPatchRequest;
import com.school_management_webapi.dto.request.AttendanceRecordUpdateRequest;
import com.school_management_webapi.dto.response.AttendanceRecordResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.AttendanceStatus;

public interface AttendanceRecordService {

	AttendanceRecordResponse create(UUID userId, AttendanceRecordCreateRequest request);

	AttendanceRecordResponse getById(UUID userId, UUID id);

	PagedResponse<AttendanceRecordResponse> list(UUID userId, UUID branchId, AttendanceStatus status, UUID classGroupId, UUID studentId, LocalDate attendanceDateFrom, LocalDate attendanceDateTo, String search, Pageable pageable);

	AttendanceRecordResponse update(UUID userId, UUID id, AttendanceRecordUpdateRequest request);

	AttendanceRecordResponse patch(UUID userId, UUID id, AttendanceRecordPatchRequest request);

	void delete(UUID userId, UUID id);
}

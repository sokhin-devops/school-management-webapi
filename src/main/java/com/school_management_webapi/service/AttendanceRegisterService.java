package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.dto.request.AttendanceRegisterRequest;
import com.school_management_webapi.dto.response.AttendanceRegisterResponse;

public interface AttendanceRegisterService {

	AttendanceRegisterResponse getRegister(UUID userId, UUID branchId, UUID classGroupId, LocalDate attendanceDate,
			String session);

	AttendanceRegisterResponse saveRegister(UUID userId, AttendanceRegisterRequest request);
}

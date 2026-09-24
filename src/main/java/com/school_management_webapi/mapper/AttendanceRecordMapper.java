package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.AttendanceRecordCreateRequest;
import com.school_management_webapi.dto.request.AttendanceRecordPatchRequest;
import com.school_management_webapi.dto.request.AttendanceRecordUpdateRequest;
import com.school_management_webapi.dto.response.AttendanceRecordResponse;
import com.school_management_webapi.entity.AttendanceRecord;

public final class AttendanceRecordMapper {

	private AttendanceRecordMapper() {
	}

	public static AttendanceRecord toEntity(AttendanceRecordCreateRequest request) {
		return AttendanceRecord.builder()
				.branchId(request.branchId())
				.classGroupId(request.classGroupId())
				.studentId(request.studentId())
				.attendanceDate(request.attendanceDate())
				.session(request.session())
				.status(request.status())
				.note(request.note())
				.build();
	}

	public static void updateEntity(AttendanceRecord entity, AttendanceRecordUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setClassGroupId(request.classGroupId());
		entity.setStudentId(request.studentId());
		entity.setAttendanceDate(request.attendanceDate());
		entity.setSession(request.session());
		entity.setStatus(request.status());
		entity.setNote(request.note());
	}

	public static void patchEntity(AttendanceRecord entity, AttendanceRecordPatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.classGroupId() != null) {
			entity.setClassGroupId(request.classGroupId());
		}

		if (request.studentId() != null) {
			entity.setStudentId(request.studentId());
		}

		if (request.attendanceDate() != null) {
			entity.setAttendanceDate(request.attendanceDate());
		}

		if (request.session() != null) {
			entity.setSession(request.session());
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}

		if (request.note() != null) {
			entity.setNote(request.note());
		}
	}

	public static AttendanceRecordResponse toResponse(AttendanceRecord entity) {
		return new AttendanceRecordResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getClassGroupId(),
				entity.getStudentId(),
				entity.getAttendanceDate(),
				entity.getSession(),
				entity.getStatus(),
				entity.getNote(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

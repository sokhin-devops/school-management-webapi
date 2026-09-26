package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.AttendanceEntryRequest;
import com.school_management_webapi.dto.request.AttendanceRegisterRequest;
import com.school_management_webapi.dto.response.AttendanceRegisterResponse;
import com.school_management_webapi.entity.AttendanceRecord;
import com.school_management_webapi.entity.AttendanceStatus;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.mapper.AttendanceRecordMapper;
import com.school_management_webapi.repository.AttendanceRecordRepository;
import com.school_management_webapi.entity.ClassGroup;
import com.school_management_webapi.repository.ClassGroupRepository;

import lombok.RequiredArgsConstructor;

/**
 * Taking a register: one class, one sitting, every student at once.
 *
 * The per-record endpoints remain for corrections afterwards, but this is how
 * attendance is actually entered, and doing it in one transaction is what keeps
 * a half-marked class from being possible.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceRegisterServiceImpl implements AttendanceRegisterService {

	private final AttendanceRecordRepository attendanceRecordRepository;
	private final BranchScopeService branchScopeService;
	private final NotificationService notificationService;
	private final ClassGroupRepository classGroupRepository;

	@Override
	@Transactional(readOnly = true)
	public AttendanceRegisterResponse getRegister(UUID userId, UUID branchId, UUID classGroupId,
			LocalDate attendanceDate, String session) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(branchId, tenantId);

		String sitting = normalise(session);
		List<AttendanceRecord> records = attendanceRecordRepository
				.findByBranchIdAndClassGroupIdAndAttendanceDateAndSession(branchId, classGroupId, attendanceDate,
						sitting);
		return toResponse(branchId, classGroupId, attendanceDate, sitting, records);
	}

	@Override
	public AttendanceRegisterResponse saveRegister(UUID userId, AttendanceRegisterRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		rejectDuplicateStudents(request.entries());

		String sitting = normalise(request.session());

		// Replaced rather than merged, so the saved register is exactly the one on
		// screen when it was submitted.
		attendanceRecordRepository.deleteByBranchIdAndClassGroupIdAndAttendanceDateAndSession(
				request.branchId(), request.classGroupId(), request.attendanceDate(), sitting);
		attendanceRecordRepository.flush();

		List<AttendanceRecord> saved = attendanceRecordRepository.saveAll(request.entries().stream()
				.map(entry -> AttendanceRecord.builder()
						.branchId(request.branchId())
						.classGroupId(request.classGroupId())
						.studentId(entry.studentId())
						.attendanceDate(request.attendanceDate())
						.session(sitting)
						.status(entry.status())
						.note(entry.note())
						.build())
				.toList());

		String className = classGroupRepository.findById(request.classGroupId()).map(ClassGroup::getName)
				.orElse("A class");
		notificationService.notifySchool(userId, NotificationEvent.ATTENDANCE_SUBMITTED,
				className + ": register taken for " + request.attendanceDate()
						+ (sitting.isEmpty() ? "" : " (" + sitting + ")") + ".",
				"/attendance");

		return toResponse(request.branchId(), request.classGroupId(), request.attendanceDate(), sitting, saved);
	}

	/**
	 * A class taken once a day has no session name. Storing that as an empty
	 * string rather than null keeps it out of the three-valued logic every query
	 * against this table would otherwise need.
	 */
	private String normalise(String session) {
		return session == null ? "" : session.trim();
	}

	private void rejectDuplicateStudents(List<AttendanceEntryRequest> entries) {
		Set<UUID> seen = new HashSet<>();
		List<UUID> duplicates = new ArrayList<>();
		for (AttendanceEntryRequest entry : entries) {
			if (!seen.add(entry.studentId())) {
				duplicates.add(entry.studentId());
			}
		}
		if (!duplicates.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "DUPLICATE_STUDENT_ATTENDANCE",
					"A student appears more than once in the register: " + duplicates.get(0));
		}
	}

	private AttendanceRegisterResponse toResponse(UUID branchId, UUID classGroupId, LocalDate attendanceDate,
			String session, List<AttendanceRecord> records) {
		return new AttendanceRegisterResponse(
				branchId,
				classGroupId,
				attendanceDate,
				session,
				count(records, AttendanceStatus.PRESENT),
				count(records, AttendanceStatus.ABSENT),
				count(records, AttendanceStatus.LATE),
				count(records, AttendanceStatus.EXCUSED),
				records.stream().map(AttendanceRecordMapper::toResponse).toList());
	}

	private int count(List<AttendanceRecord> records, AttendanceStatus status) {
		return (int) records.stream().filter(record -> record.getStatus() == status).count();
	}
}

package com.school_management_webapi.service;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.response.SettingsResponse;
import com.school_management_webapi.entity.AuditAction;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.entity.TenantUser;
import com.school_management_webapi.entity.TenantUserRole;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.mapper.AcademicYearMapper;
import com.school_management_webapi.mapper.AssessmentMapper;
import com.school_management_webapi.mapper.AttendanceRecordMapper;
import com.school_management_webapi.mapper.BranchMapper;
import com.school_management_webapi.mapper.ClassGroupMapper;
import com.school_management_webapi.mapper.ExpenseMapper;
import com.school_management_webapi.mapper.FeeMapper;
import com.school_management_webapi.mapper.LevelMapper;
import com.school_management_webapi.mapper.ParentMapper;
import com.school_management_webapi.mapper.PaymentMapper;
import com.school_management_webapi.mapper.ProgramMapper;
import com.school_management_webapi.mapper.RoomMapper;
import com.school_management_webapi.mapper.SchoolMapper;
import com.school_management_webapi.mapper.StudentMapper;
import com.school_management_webapi.mapper.SubjectMapper;
import com.school_management_webapi.mapper.TeacherMapper;
import com.school_management_webapi.repository.AcademicYearRepository;
import com.school_management_webapi.repository.AssessmentRepository;
import com.school_management_webapi.repository.AttendanceRecordRepository;
import com.school_management_webapi.repository.BranchRepository;
import com.school_management_webapi.repository.ClassGroupRepository;
import com.school_management_webapi.repository.ExpenseRepository;
import com.school_management_webapi.repository.FeeRepository;
import com.school_management_webapi.repository.LevelRepository;
import com.school_management_webapi.repository.ParentRepository;
import com.school_management_webapi.repository.PaymentRepository;
import com.school_management_webapi.repository.ProgramRepository;
import com.school_management_webapi.repository.RoomRepository;
import com.school_management_webapi.repository.SchoolRepository;
import com.school_management_webapi.repository.StudentRepository;
import com.school_management_webapi.repository.SubjectRepository;
import com.school_management_webapi.repository.TeacherRepository;
import com.school_management_webapi.repository.TenantUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 69-system-settings.md: what the installation is, and the operations that
 * act on a whole school at once - export everything, delete everything.
 */
@Service
@RequiredArgsConstructor
public class SystemService {

	private final Optional<BuildProperties> buildProperties;
	private final Environment environment;
	private final DataSource dataSource;

	private final TenantAuthorizationService tenantAuthorizationService;
	private final TenantUserRepository tenantUserRepository;
	private final AuditService auditService;

	private final SchoolRepository schoolRepository;
	private final BranchRepository branchRepository;
	private final AcademicYearRepository academicYearRepository;
	private final ProgramRepository programRepository;
	private final LevelRepository levelRepository;
	private final ClassGroupRepository classGroupRepository;
	private final SubjectRepository subjectRepository;
	private final RoomRepository roomRepository;
	private final TeacherRepository teacherRepository;
	private final StudentRepository studentRepository;
	private final ParentRepository parentRepository;
	private final FeeRepository feeRepository;
	private final PaymentRepository paymentRepository;
	private final ExpenseRepository expenseRepository;
	private final AssessmentRepository assessmentRepository;
	private final AttendanceRecordRepository attendanceRecordRepository;

	@Value("${spring.application.name:school-management-webapi}")
	private String applicationName;

	public SettingsResponse.SystemInfo info() {
		String version = buildProperties.map(BuildProperties::getVersion).orElse("development build");
		String[] profiles = environment.getActiveProfiles();
		String env = profiles.length == 0 ? "Development" : String.join(", ", profiles);
		LocalDateTime startedAt = LocalDateTime.ofInstant(
				Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()), ZoneId.systemDefault());
		return new SettingsResponse.SystemInfo(applicationName + " " + version, env, database(), startedAt,
				LocalDateTime.now());
	}

	/**
	 * Everything the school has entered, as one JSON document. The same shapes
	 * the API serves, so an export reads like the API it came from.
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> export(UUID userId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		List<UUID> schoolIds = schoolRepository.findIdsByTenantId(tenantId);
		List<UUID> branchIds = branchRepository.findIdsByTenantId(tenantId);

		Map<String, Object> export = new LinkedHashMap<>();
		export.put("exportedAt", LocalDateTime.now());
		export.put("schools", schoolRepository.findByTenantIdOrderByCreatedAtAsc(tenantId).stream()
				.map(SchoolMapper::toResponse).toList());
		export.put("branches", branchRepository.findAllByTenantId(tenantId).stream()
				.map(BranchMapper::toResponse).toList());
		export.put("academicYears", academicYearRepository.findAllByTenantId(tenantId).stream()
				.map(AcademicYearMapper::toResponse).toList());
		export.put("programs", programRepository.findByBranchIdIn(branchIds).stream()
				.map(ProgramMapper::toResponse).toList());
		export.put("levels", levelRepository.findByBranchIdIn(branchIds).stream()
				.map(LevelMapper::toResponse).toList());
		export.put("classes", classGroupRepository.findByBranchIdIn(branchIds).stream()
				.map(ClassGroupMapper::toResponse).toList());
		export.put("subjects", subjectRepository.findByBranchIdIn(branchIds).stream()
				.map(SubjectMapper::toResponse).toList());
		export.put("rooms", roomRepository.findByBranchIdIn(branchIds).stream()
				.map(RoomMapper::toResponse).toList());
		export.put("teachers", teacherRepository.findByBranchIdIn(branchIds).stream()
				.map(TeacherMapper::toResponse).toList());
		export.put("students", studentRepository.findBySchoolIdIn(schoolIds).stream()
				.map(StudentMapper::toResponse).toList());
		export.put("parents", parentRepository.findByBranchIdIn(branchIds).stream()
				.map(ParentMapper::toResponse).toList());
		export.put("fees", feeRepository.findByBranchIdIn(branchIds).stream()
				.map(FeeMapper::toResponse).toList());
		export.put("payments", paymentRepository.findByBranchIdIn(branchIds).stream()
				.map(PaymentMapper::toResponse).toList());
		export.put("expenses", expenseRepository.findByBranchIdIn(branchIds).stream()
				.map(ExpenseMapper::toResponse).toList());
		export.put("assessments", assessmentRepository.findByBranchIdIn(branchIds).stream()
				.map(AssessmentMapper::toResponse).toList());
		export.put("attendance", attendanceRecordRepository.findByBranchIdIn(branchIds).stream()
				.map(AttendanceRecordMapper::toResponse).toList());

		auditService.record(userId, AuditAction.DATA_EXPORTED, null);
		return export;
	}

	/**
	 * Deletes every student, staff, academic and finance record, keeping the
	 * school, its branches, its academic years, its people's accounts and its
	 * subscription - what it takes to start entering again.
	 *
	 * Owner only, and only with the school's name typed as confirmation. Every
	 * record is soft-deleted, so the rows stay in the database with a deletion
	 * time rather than being destroyed outright.
	 */
	@Transactional
	public Map<String, Integer> deleteAllData(UUID userId, String confirmation) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		TenantUser membership = tenantUserRepository.findByTenantIdAndUserId(tenantId, userId)
				.orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "OWNER_ONLY",
						"Only the school's owner can delete its data."));
		if (membership.getRole() != TenantUserRole.OWNER) {
			throw new ApiException(HttpStatus.FORBIDDEN, "OWNER_ONLY", "Only the school's owner can delete its data.");
		}

		List<School> schools = schoolRepository.findByTenantIdOrderByCreatedAtAsc(tenantId);
		String expected = schools.isEmpty() ? "" : schools.get(0).getName().trim();
		if (expected.isEmpty() || !expected.equalsIgnoreCase(confirmation.trim())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "CONFIRMATION_MISMATCH",
					"Type the school's name exactly to confirm.");
		}

		List<UUID> schoolIds = schools.stream().map(School::getId).toList();
		List<UUID> branchIds = branchRepository.findIdsByTenantId(tenantId);

		// Children before the records they point at, so nothing is ever left
		// pointing at a row that has gone.
		Map<String, Integer> deleted = new LinkedHashMap<>();
		deleted.put("attendance", deleteAll(attendanceRecordRepository.findByBranchIdIn(branchIds), attendanceRecordRepository));
		deleted.put("assessments", deleteAll(assessmentRepository.findByBranchIdIn(branchIds), assessmentRepository));
		deleted.put("payments", deleteAll(paymentRepository.findByBranchIdIn(branchIds), paymentRepository));
		deleted.put("fees", deleteAll(feeRepository.findByBranchIdIn(branchIds), feeRepository));
		deleted.put("expenses", deleteAll(expenseRepository.findByBranchIdIn(branchIds), expenseRepository));
		deleted.put("parents", deleteAll(parentRepository.findByBranchIdIn(branchIds), parentRepository));
		deleted.put("students", deleteAll(studentRepository.findBySchoolIdIn(schoolIds), studentRepository));
		deleted.put("teachers", deleteAll(teacherRepository.findByBranchIdIn(branchIds), teacherRepository));
		deleted.put("classes", deleteAll(classGroupRepository.findByBranchIdIn(branchIds), classGroupRepository));
		deleted.put("subjects", deleteAll(subjectRepository.findByBranchIdIn(branchIds), subjectRepository));
		deleted.put("levels", deleteAll(levelRepository.findByBranchIdIn(branchIds), levelRepository));
		deleted.put("programs", deleteAll(programRepository.findByBranchIdIn(branchIds), programRepository));
		deleted.put("rooms", deleteAll(roomRepository.findByBranchIdIn(branchIds), roomRepository));

		int total = deleted.values().stream().mapToInt(Integer::intValue).sum();
		auditService.record(userId, AuditAction.DATA_DELETED, total + " records deleted");
		return deleted;
	}

	private static <T> int deleteAll(List<T> rows, org.springframework.data.repository.CrudRepository<T, ?> repository) {
		repository.deleteAll(rows);
		return rows.size();
	}

	private String database() {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			return meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
		} catch (Exception unreachable) {
			return "Unavailable";
		}
	}
}

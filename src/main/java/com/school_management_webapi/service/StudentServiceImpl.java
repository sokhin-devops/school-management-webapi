package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.StudentCreateRequest;
import com.school_management_webapi.dto.request.StudentPatchRequest;
import com.school_management_webapi.dto.request.StudentUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.StudentResponse;
import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.Student;
import com.school_management_webapi.entity.StudentStatus;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.StudentMapper;
import com.school_management_webapi.repository.SchoolRepository;
import com.school_management_webapi.repository.StudentRepository;
import com.school_management_webapi.repository.StudentSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

	private final StudentRepository studentRepository;
	private final SchoolRepository schoolRepository;
	private final TenantAuthorizationService tenantAuthorizationService;
	private final SubscriptionLimitService subscriptionLimitService;
	private final NotificationService notificationService;

	@Override
	public StudentResponse create(UUID userId, StudentCreateRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		requireSchoolInTenant(request.schoolId(), tenantId);

		subscriptionLimitService.checkLimit(tenantId, LimitType.STUDENTS, countStudentsForTenant(tenantId));

		if (studentRepository.existsByStudentCodeAndSchoolId(request.studentCode(), request.schoolId())) {
			throw new DuplicateResourceException(
					"A student with code '" + request.studentCode() + "' already exists for this school");
		}

		Student student = StudentMapper.toEntity(request);
		StudentResponse created = StudentMapper.toResponse(studentRepository.saveAndFlush(student));
		notificationService.notifySchool(userId, NotificationEvent.STUDENT_ENROLLED,
				request.firstName() + " " + request.lastName() + " (" + request.studentCode() + ") was enrolled.",
				"/people/students");
		return created;
	}

	@Override
	@Transactional(readOnly = true)
	public StudentResponse getById(UUID userId, UUID id) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return StudentMapper.toResponse(findStudentInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<StudentResponse> list(UUID userId, UUID schoolId, StudentStatus status, Gender gender,
			String search, Pageable pageable) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		if (schoolId != null) {
			requireSchoolInTenant(schoolId, tenantId);
		}

		List<UUID> allowedSchoolIds = schoolRepository.findIdsByTenantId(tenantId);
		Page<Student> page = studentRepository.findAll(
				StudentSpecification.filterBy(allowedSchoolIds, schoolId, status, gender, search), pageable);
		return PagedResponse.of(page.map(StudentMapper::toResponse));
	}

	@Override
	public StudentResponse update(UUID userId, UUID id, StudentUpdateRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		Student student = findStudentInTenantOrThrow(id, tenantId);
		requireSchoolInTenant(request.schoolId(), tenantId);
		ensureStudentCodeIsAvailable(id, request.studentCode(), request.schoolId());

		StudentMapper.updateEntity(student, request);
		return StudentMapper.toResponse(studentRepository.saveAndFlush(student));
	}

	@Override
	public StudentResponse patch(UUID userId, UUID id, StudentPatchRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		Student student = findStudentInTenantOrThrow(id, tenantId);

		String newStudentCode = request.studentCode() != null ? request.studentCode() : student.getStudentCode();
		UUID newSchoolId = request.schoolId() != null ? request.schoolId() : student.getSchoolId();
		if (request.schoolId() != null) {
			requireSchoolInTenant(request.schoolId(), tenantId);
		}
		ensureStudentCodeIsAvailable(id, newStudentCode, newSchoolId);

		StudentMapper.patchEntity(student, request);
		return StudentMapper.toResponse(studentRepository.saveAndFlush(student));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		studentRepository.delete(findStudentInTenantOrThrow(id, tenantId));
	}

	/**
	 * A student outside the tenant of the caller is reported as not found rather
	 * than forbidden, so ids cannot be probed across tenants.
	 */
	private Student findStudentInTenantOrThrow(UUID id, UUID tenantId) {
		Student student = studentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

		if (!schoolRepository.existsByIdAndTenantId(student.getSchoolId(), tenantId)) {
			throw new ResourceNotFoundException("Student not found with id: " + id);
		}
		return student;
	}

	private void requireSchoolInTenant(UUID schoolId, UUID tenantId) {
		if (!schoolRepository.existsByIdAndTenantId(schoolId, tenantId)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "SCHOOL_NOT_FOUND", "School not found.");
		}
	}

	private long countStudentsForTenant(UUID tenantId) {
		List<UUID> schoolIds = schoolRepository.findIdsByTenantId(tenantId);
		return schoolIds.isEmpty() ? 0L : studentRepository.countBySchoolIdIn(schoolIds);
	}

	private void ensureStudentCodeIsAvailable(UUID studentId, String studentCode, UUID schoolId) {
		studentRepository.findByStudentCodeAndSchoolId(studentCode, schoolId)
				.filter(existing -> !existing.getId().equals(studentId))
				.ifPresent(existing -> {
					throw new DuplicateResourceException(
							"A student with code '" + studentCode + "' already exists for this school");
				});
	}
}

package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.StudentMapper;
import com.school_management_webapi.repository.StudentRepository;
import com.school_management_webapi.repository.StudentSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

	private final StudentRepository studentRepository;

	@Override
	public StudentResponse create(StudentCreateRequest request) {
		if (studentRepository.existsByStudentCodeAndSchoolId(request.studentCode(), request.schoolId())) {
			throw new DuplicateResourceException(
					"A student with code '" + request.studentCode() + "' already exists for this school");
		}

		Student student = StudentMapper.toEntity(request);
		Student saved = studentRepository.save(student);
		return StudentMapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public StudentResponse getById(UUID id) {
		return StudentMapper.toResponse(findStudentOrThrow(id));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<StudentResponse> list(UUID schoolId, StudentStatus status, Gender gender, String search,
			Pageable pageable) {
		Page<Student> page = studentRepository.findAll(
				StudentSpecification.filterBy(schoolId, status, gender, search), pageable);
		return PagedResponse.of(page.map(StudentMapper::toResponse));
	}

	@Override
	public StudentResponse update(UUID id, StudentUpdateRequest request) {
		Student student = findStudentOrThrow(id);
		ensureStudentCodeIsAvailable(id, request.studentCode(), request.schoolId());

		StudentMapper.updateEntity(student, request);
		return StudentMapper.toResponse(studentRepository.save(student));
	}

	@Override
	public StudentResponse patch(UUID id, StudentPatchRequest request) {
		Student student = findStudentOrThrow(id);

		String newStudentCode = request.studentCode() != null ? request.studentCode() : student.getStudentCode();
		UUID newSchoolId = request.schoolId() != null ? request.schoolId() : student.getSchoolId();
		ensureStudentCodeIsAvailable(id, newStudentCode, newSchoolId);

		StudentMapper.patchEntity(student, request);
		return StudentMapper.toResponse(studentRepository.save(student));
	}

	@Override
	public void delete(UUID id) {
		Student student = findStudentOrThrow(id);
		studentRepository.delete(student);
	}

	private Student findStudentOrThrow(UUID id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
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

package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.StudentCreateRequest;
import com.school_management_webapi.dto.request.StudentPatchRequest;
import com.school_management_webapi.dto.request.StudentUpdateRequest;
import com.school_management_webapi.dto.response.StudentResponse;
import com.school_management_webapi.entity.Student;
import com.school_management_webapi.entity.StudentStatus;

public final class StudentMapper {

	private StudentMapper() {
	}

	public static Student toEntity(StudentCreateRequest request) {
		return Student.builder()
				.schoolId(request.schoolId())
				.studentCode(request.studentCode())
				.firstName(request.firstName())
				.middleName(request.middleName())
				.lastName(request.lastName())
				.preferredName(request.preferredName())
				.gender(request.gender())
				.dateOfBirth(request.dateOfBirth())
				.nationality(request.nationality())
				.nationalId(request.nationalId())
				.photoUrl(request.photoUrl())
				.email(request.email())
				.phone(request.phone())
				.admissionDate(request.admissionDate())
				.status(StudentStatus.ACTIVE)
				.build();
	}

	public static void updateEntity(Student student, StudentUpdateRequest request) {
		student.setSchoolId(request.schoolId());
		student.setStudentCode(request.studentCode());
		student.setFirstName(request.firstName());
		student.setMiddleName(request.middleName());
		student.setLastName(request.lastName());
		student.setPreferredName(request.preferredName());
		student.setGender(request.gender());
		student.setDateOfBirth(request.dateOfBirth());
		student.setNationality(request.nationality());
		student.setNationalId(request.nationalId());
		student.setPhotoUrl(request.photoUrl());
		student.setEmail(request.email());
		student.setPhone(request.phone());
		student.setAdmissionDate(request.admissionDate());
		student.setStatus(request.status());
	}

	public static void patchEntity(Student student, StudentPatchRequest request) {
		if (request.schoolId() != null) {
			student.setSchoolId(request.schoolId());
		}
		if (request.studentCode() != null) {
			student.setStudentCode(request.studentCode());
		}
		if (request.firstName() != null) {
			student.setFirstName(request.firstName());
		}
		if (request.middleName() != null) {
			student.setMiddleName(request.middleName());
		}
		if (request.lastName() != null) {
			student.setLastName(request.lastName());
		}
		if (request.preferredName() != null) {
			student.setPreferredName(request.preferredName());
		}
		if (request.gender() != null) {
			student.setGender(request.gender());
		}
		if (request.dateOfBirth() != null) {
			student.setDateOfBirth(request.dateOfBirth());
		}
		if (request.nationality() != null) {
			student.setNationality(request.nationality());
		}
		if (request.nationalId() != null) {
			student.setNationalId(request.nationalId());
		}
		if (request.photoUrl() != null) {
			student.setPhotoUrl(request.photoUrl());
		}
		if (request.email() != null) {
			student.setEmail(request.email());
		}
		if (request.phone() != null) {
			student.setPhone(request.phone());
		}
		if (request.admissionDate() != null) {
			student.setAdmissionDate(request.admissionDate());
		}
		if (request.status() != null) {
			student.setStatus(request.status());
		}
	}

	public static StudentResponse toResponse(Student student) {
		String fullName = java.util.stream.Stream
				.of(student.getFirstName(), student.getMiddleName(), student.getLastName())
				.filter(part -> part != null && !part.isBlank())
				.reduce((a, b) -> a + " " + b)
				.orElse(null);

		return new StudentResponse(
				student.getId(),
				student.getSchoolId(),
				student.getStudentCode(),
				student.getFirstName(),
				student.getMiddleName(),
				student.getLastName(),
				student.getPreferredName(),
				fullName,
				student.getGender(),
				student.getDateOfBirth(),
				student.getNationality(),
				student.getNationalId(),
				student.getPhotoUrl(),
				student.getEmail(),
				student.getPhone(),
				student.getAdmissionDate(),
				student.getStatus(),
				student.getCreatedAt(),
				student.getUpdatedAt());
	}
}

package com.school_management_webapi.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.school_management_webapi.dto.request.TeacherCreateRequest;
import com.school_management_webapi.dto.request.TeacherPatchRequest;
import com.school_management_webapi.dto.request.TeacherUpdateRequest;
import com.school_management_webapi.dto.response.TeacherResponse;
import com.school_management_webapi.entity.Teacher;

public final class TeacherMapper {

	private TeacherMapper() {
	}

	public static Teacher toEntity(TeacherCreateRequest request) {
		return Teacher.builder()
				.branchId(request.branchId())
				.employeeNumber(request.employeeNumber())
				.firstName(request.firstName())
				.lastName(request.lastName())
				.gender(request.gender())
				.dateOfBirth(request.dateOfBirth())
				.email(request.email())
				.phone(request.phone())
				.department(request.department())
				.hireDate(request.hireDate())
				.subjectIds(copyOf(request.subjectIds()))
				.status(request.status())
				.build();
	}

	public static void updateEntity(Teacher entity, TeacherUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setEmployeeNumber(request.employeeNumber());
		entity.setFirstName(request.firstName());
		entity.setLastName(request.lastName());
		entity.setGender(request.gender());
		entity.setDateOfBirth(request.dateOfBirth());
		entity.setEmail(request.email());
		entity.setPhone(request.phone());
		entity.setDepartment(request.department());
		entity.setHireDate(request.hireDate());
		entity.setSubjectIds(copyOf(request.subjectIds()));
		entity.setStatus(request.status());
	}

	public static void patchEntity(Teacher entity, TeacherPatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.employeeNumber() != null) {
			entity.setEmployeeNumber(request.employeeNumber());
		}

		if (request.firstName() != null) {
			entity.setFirstName(request.firstName());
		}

		if (request.lastName() != null) {
			entity.setLastName(request.lastName());
		}

		if (request.gender() != null) {
			entity.setGender(request.gender());
		}

		if (request.dateOfBirth() != null) {
			entity.setDateOfBirth(request.dateOfBirth());
		}

		if (request.email() != null) {
			entity.setEmail(request.email());
		}

		if (request.phone() != null) {
			entity.setPhone(request.phone());
		}

		if (request.department() != null) {
			entity.setDepartment(request.department());
		}

		if (request.hireDate() != null) {
			entity.setHireDate(request.hireDate());
		}

		if (request.subjectIds() != null) {
			entity.setSubjectIds(copyOf(request.subjectIds()));
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}
	}

	public static TeacherResponse toResponse(Teacher entity) {
		return new TeacherResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getEmployeeNumber(),
				entity.getFirstName(),
				entity.getLastName(),
				entity.getGender(),
				entity.getDateOfBirth(),
				entity.getEmail(),
				entity.getPhone(),
				entity.getDepartment(),
				entity.getHireDate(),
				entity.getSubjectIds(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}

	/**
	 * A replaced collection is copied rather than handed straight over, because
	 * Hibernate tracks the list instance it gave out and a request's list is not
	 * that one.
	 */
	private static List<UUID> copyOf(List<UUID> ids) {
		return ids == null ? new ArrayList<>() : new ArrayList<>(ids);
	}
}

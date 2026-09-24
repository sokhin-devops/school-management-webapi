package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.AssessmentCreateRequest;
import com.school_management_webapi.dto.request.AssessmentPatchRequest;
import com.school_management_webapi.dto.request.AssessmentUpdateRequest;
import com.school_management_webapi.dto.response.AssessmentResponse;
import com.school_management_webapi.entity.Assessment;

public final class AssessmentMapper {

	private AssessmentMapper() {
	}

	public static Assessment toEntity(AssessmentCreateRequest request) {
		return Assessment.builder()
				.branchId(request.branchId())
				.academicYearId(request.academicYearId())
				.classGroupId(request.classGroupId())
				.subjectId(request.subjectId())
				.name(request.name())
				.type(request.type())
				.assessedOn(request.assessedOn())
				.maxScore(request.maxScore())
				.build();
	}

	public static void updateEntity(Assessment entity, AssessmentUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setAcademicYearId(request.academicYearId());
		entity.setClassGroupId(request.classGroupId());
		entity.setSubjectId(request.subjectId());
		entity.setName(request.name());
		entity.setType(request.type());
		entity.setAssessedOn(request.assessedOn());
		entity.setMaxScore(request.maxScore());
	}

	public static void patchEntity(Assessment entity, AssessmentPatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.academicYearId() != null) {
			entity.setAcademicYearId(request.academicYearId());
		}

		if (request.classGroupId() != null) {
			entity.setClassGroupId(request.classGroupId());
		}

		if (request.subjectId() != null) {
			entity.setSubjectId(request.subjectId());
		}

		if (request.name() != null) {
			entity.setName(request.name());
		}

		if (request.type() != null) {
			entity.setType(request.type());
		}

		if (request.assessedOn() != null) {
			entity.setAssessedOn(request.assessedOn());
		}

		if (request.maxScore() != null) {
			entity.setMaxScore(request.maxScore());
		}
	}

	public static AssessmentResponse toResponse(Assessment entity) {
		return new AssessmentResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getAcademicYearId(),
				entity.getClassGroupId(),
				entity.getSubjectId(),
				entity.getName(),
				entity.getType(),
				entity.getAssessedOn(),
				entity.getMaxScore(),
				entity.getAverageScore(),
				entity.getGraded(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

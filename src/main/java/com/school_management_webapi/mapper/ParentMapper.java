package com.school_management_webapi.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.school_management_webapi.dto.request.ParentCreateRequest;
import com.school_management_webapi.dto.request.ParentPatchRequest;
import com.school_management_webapi.dto.request.ParentUpdateRequest;
import com.school_management_webapi.dto.response.ParentResponse;
import com.school_management_webapi.entity.Parent;

public final class ParentMapper {

	private ParentMapper() {
	}

	public static Parent toEntity(ParentCreateRequest request) {
		return Parent.builder()
				.branchId(request.branchId())
				.firstName(request.firstName())
				.lastName(request.lastName())
				.relationship(request.relationship())
				.email(request.email())
				.phone(request.phone())
				.studentIds(copyOf(request.studentIds()))
				.status(request.status())
				.build();
	}

	public static void updateEntity(Parent entity, ParentUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setFirstName(request.firstName());
		entity.setLastName(request.lastName());
		entity.setRelationship(request.relationship());
		entity.setEmail(request.email());
		entity.setPhone(request.phone());
		entity.setStudentIds(copyOf(request.studentIds()));
		entity.setStatus(request.status());
	}

	public static void patchEntity(Parent entity, ParentPatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.firstName() != null) {
			entity.setFirstName(request.firstName());
		}

		if (request.lastName() != null) {
			entity.setLastName(request.lastName());
		}

		if (request.relationship() != null) {
			entity.setRelationship(request.relationship());
		}

		if (request.email() != null) {
			entity.setEmail(request.email());
		}

		if (request.phone() != null) {
			entity.setPhone(request.phone());
		}

		if (request.studentIds() != null) {
			entity.setStudentIds(copyOf(request.studentIds()));
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}
	}

	public static ParentResponse toResponse(Parent entity) {
		return new ParentResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getFirstName(),
				entity.getLastName(),
				entity.getRelationship(),
				entity.getEmail(),
				entity.getPhone(),
				entity.getStudentIds(),
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

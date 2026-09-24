package com.school_management_webapi.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.school_management_webapi.dto.request.RolePermissionRequest;
import com.school_management_webapi.dto.response.RolePermissionResponse;
import com.school_management_webapi.dto.response.RoleResponse;
import com.school_management_webapi.entity.PermissionAction;
import com.school_management_webapi.entity.Role;
import com.school_management_webapi.entity.RolePermission;

public final class RoleMapper {

	private RoleMapper() {
	}

	/**
	 * The grid is sent grouped by module and stored as one row per action, so the
	 * two representations are converted at this boundary and nowhere else.
	 */
	public static Set<RolePermission> toPermissions(List<RolePermissionRequest> requests) {
		Set<RolePermission> permissions = new LinkedHashSet<>();
		if (requests == null) {
			return permissions;
		}
		for (RolePermissionRequest request : requests) {
			for (PermissionAction action : request.actions()) {
				permissions.add(new RolePermission(request.module(), action));
			}
		}
		return permissions;
	}

	public static RoleResponse toResponse(Role role, long userCount) {
		Map<String, List<PermissionAction>> grouped = new LinkedHashMap<>();
		for (RolePermission permission : role.getPermissions()) {
			grouped.computeIfAbsent(permission.getModule(), module -> new ArrayList<>()).add(permission.getAction());
		}

		List<RolePermissionResponse> permissions = grouped.entrySet().stream()
				.map(entry -> new RolePermissionResponse(entry.getKey(), entry.getValue()))
				.toList();

		return new RoleResponse(
				role.getId(),
				role.getName(),
				role.isDefaultRole(),
				role.getDefaultType(),
				permissions,
				List.copyOf(role.getBranchIds()),
				userCount,
				role.getCreatedAt(),
				role.getUpdatedAt());
	}
}

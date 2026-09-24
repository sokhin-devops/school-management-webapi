package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.DefaultRoleType;

public record RoleResponse(
		UUID id,
		String name,
		boolean isDefault,
		DefaultRoleType defaultType,
		List<RolePermissionResponse> permissions,
		List<UUID> branchIds,
		/** How many users hold this role, so the list can warn before a delete. */
		long userCount,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

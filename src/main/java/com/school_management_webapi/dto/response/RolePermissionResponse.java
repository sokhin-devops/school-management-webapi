package com.school_management_webapi.dto.response;

import java.util.List;

import com.school_management_webapi.entity.PermissionAction;

public record RolePermissionResponse(
		String module,
		List<PermissionAction> actions) {
}

package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.TenantUserRole;
import com.school_management_webapi.entity.UserStatus;

/**
 * A person with access to the tenant, as Settings sees them: the account, the
 * role they hold and the branches they may work in, on one row.
 */
public record TenantUserResponse(
		UUID id,
		UUID userId,
		String fullName,
		String email,
		UUID roleId,
		String roleName,
		TenantUserRole membership,
		List<UUID> branchIds,
		UserStatus status,
		LocalDateTime lastLoginAt,
		LocalDateTime createdAt) {
}

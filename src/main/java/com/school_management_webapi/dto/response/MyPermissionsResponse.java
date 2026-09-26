package com.school_management_webapi.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * What the signed-in user may do, so the web app can hide what would be refused
 * rather than offer a button that returns 403.
 */
public record MyPermissionsResponse(
		UUID roleId,
		String roleName,
		/** True when nothing is withheld: the owner, or a member with no role yet. */
		boolean unrestricted,
		/** The tenant's owner - the one person some operations, such as deleting all data, are kept for. */
		boolean owner,
		List<RolePermissionResponse> permissions,
		/** The plan's enabled features; null when there is no live plan and nothing is gated. */
		List<String> features) {
}

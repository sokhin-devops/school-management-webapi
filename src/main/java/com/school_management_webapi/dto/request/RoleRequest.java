package com.school_management_webapi.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A custom role, created or replaced.
 *
 * The same shape serves both, because a role is small enough that a partial
 * update would only invite a half-applied permission tree.
 */
public record RoleRequest(
		@NotBlank(message = "name is required") @Size(max = 80, message = "name must be 80 characters or fewer") String name,

		@NotNull(message = "permissions is required") @Valid List<RolePermissionRequest> permissions,

		/** Empty means every branch of the tenant. */
		List<UUID> branchIds) {
}

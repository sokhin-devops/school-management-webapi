package com.school_management_webapi.dto.request;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.entity.UserStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** What Settings may change about someone else's access. */
public record TenantUserUpdateRequest(
		@NotBlank(message = "fullName is required") @Size(max = 150, message = "fullName must be 150 characters or fewer") String fullName,

		@NotNull(message = "roleId is required") UUID roleId,

		@NotNull(message = "status is required") UserStatus status,

		/** Empty means every branch of the tenant. */
		List<UUID> branchIds) {
}

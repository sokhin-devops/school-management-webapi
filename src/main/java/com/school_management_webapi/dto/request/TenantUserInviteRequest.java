package com.school_management_webapi.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * An invitation to join the tenant.
 *
 * No password: the account is created without one and the person sets it
 * through the existing forgot-password flow, so an invitation never puts a
 * credential in an email or in this request.
 */
public record TenantUserInviteRequest(
		@NotBlank(message = "fullName is required") @Size(max = 150, message = "fullName must be 150 characters or fewer") String fullName,

		@NotBlank(message = "email is required") @Email(message = "email must be a valid email address") String email,

		@NotNull(message = "roleId is required") UUID roleId,

		/** Empty means every branch of the tenant. */
		List<UUID> branchIds) {
}

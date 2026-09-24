package com.school_management_webapi.dto.request;

import java.util.List;

import com.school_management_webapi.entity.PermissionAction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * One module and what may be done to it. Grouped this way rather than as flat
 * pairs because it is how the permission tree is read and edited; the rows it
 * becomes are an implementation detail of the table underneath.
 */
public record RolePermissionRequest(
		@NotBlank(message = "module is required") String module,

		@NotNull(message = "actions is required") List<PermissionAction> actions) {
}

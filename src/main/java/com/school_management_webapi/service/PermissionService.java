package com.school_management_webapi.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.school_management_webapi.entity.PermissionAction;
import com.school_management_webapi.entity.Role;
import com.school_management_webapi.entity.RolePermission;
import com.school_management_webapi.entity.TenantUser;
import com.school_management_webapi.entity.TenantUserRole;
import com.school_management_webapi.repository.RoleRepository;
import com.school_management_webapi.dto.response.MyPermissionsResponse;
import com.school_management_webapi.dto.response.RolePermissionResponse;
import com.school_management_webapi.repository.TenantUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Whether a user may do a thing to a module.
 *
 * 64-users-and-roles.md: a role is a grid of modules crossed with view, create,
 * edit and delete, and a user holds one role.
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

	private final TenantUserRepository tenantUserRepository;
	private final RoleRepository roleRepository;
	private final PlanFeatureGate planFeatureGate;

	/**
	 * The owner of a tenant is never locked out, and neither is a member who has
	 * not been given a role yet.
	 *
	 * The second case is deliberate: every tenant created before roles existed
	 * has members with no roleId, and refusing them would lock those schools out
	 * of their own data on the day this shipped. Assigning roles is what turns
	 * the grid on for a tenant.
	 */
	public boolean isAllowed(UUID userId, String module, PermissionAction action) {
		Optional<TenantUser> membership = tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(userId);
		if (membership.isEmpty()) {
			return false;
		}

		TenantUser tenantUser = membership.get();
		if (tenantUser.getRole() == TenantUserRole.OWNER || tenantUser.getRoleId() == null) {
			return true;
		}

		return roleRepository.findById(tenantUser.getRoleId())
				.map(role -> grants(role, module, action))
				.orElse(false);
	}

	/**
	 * The caller's own grid, grouped by module the way the Settings screen draws
	 * it. An unrestricted caller returns an empty grid with the flag set, rather
	 * than every cell enumerated: the client reads the flag first.
	 */
	public MyPermissionsResponse permissionsFor(UUID userId) {
		Optional<TenantUser> membership = tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(userId);
		if (membership.isEmpty()) {
			return new MyPermissionsResponse(null, null, false, false, List.of(), null);
		}

		TenantUser tenantUser = membership.get();
		List<String> features = planFeatureGate.enabledCodes(tenantUser.getTenant().getId()).orElse(null);
		if (tenantUser.getRole() == TenantUserRole.OWNER || tenantUser.getRoleId() == null) {
			return new MyPermissionsResponse(tenantUser.getRoleId(), null, true,
					tenantUser.getRole() == TenantUserRole.OWNER, List.of(), features);
		}

		return roleRepository.findById(tenantUser.getRoleId())
				.map(role -> new MyPermissionsResponse(role.getId(), role.getName(), false, false, group(role), features))
				.orElseGet(() -> new MyPermissionsResponse(tenantUser.getRoleId(), null, false, false, List.of(), features));
	}

	private List<RolePermissionResponse> group(Role role) {
		Map<String, List<PermissionAction>> byModule = new LinkedHashMap<>();
		for (RolePermission permission : role.getPermissions()) {
			byModule.computeIfAbsent(permission.getModule(), key -> new ArrayList<>()).add(permission.getAction());
		}

		List<RolePermissionResponse> grid = new ArrayList<>();
		byModule.forEach((module, actions) -> grid.add(new RolePermissionResponse(module, actions)));
		return grid;
	}

	private boolean grants(Role role, String module, PermissionAction action) {
		for (RolePermission permission : role.getPermissions()) {
			if (module.equals(permission.getModule()) && permission.getAction() == action) {
				return true;
			}
		}
		return false;
	}
}

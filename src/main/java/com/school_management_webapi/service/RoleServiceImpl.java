package com.school_management_webapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.RoleRequest;
import com.school_management_webapi.dto.response.RoleResponse;
import com.school_management_webapi.entity.DefaultRoleType;
import com.school_management_webapi.entity.PermissionAction;
import com.school_management_webapi.entity.Role;
import com.school_management_webapi.entity.RolePermission;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.RoleMapper;
import com.school_management_webapi.repository.RoleRepository;
import com.school_management_webapi.repository.TenantUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Roles, and the five every tenant is given.
 *
 * The defaults are seeded the first time a tenant asks for its roles rather
 * than during onboarding: tenants created before this module existed have none,
 * and a lazy seed covers both those and new ones without a migration.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

	private static final List<PermissionAction> ALL_ACTIONS = List.of(
			PermissionAction.VIEW, PermissionAction.CREATE, PermissionAction.EDIT, PermissionAction.DELETE);

	/** 64-users-and-roles.md. Branch Admin is deliberately not among them. */
	private static final Map<DefaultRoleType, List<String>> DEFAULT_ROLES = Map.of(
			DefaultRoleType.OWNER,
			List.of("students", "teachers", "parents", "academic", "attendance", "finance", "reports", "settings"),
			DefaultRoleType.ACCOUNTING, List.of("students", "finance", "reports"),
			DefaultRoleType.TEACHER, List.of("students", "academic", "attendance"),
			DefaultRoleType.PARENT, List.of("students", "finance"),
			DefaultRoleType.STUDENT, List.of("academic"));

	private static final Map<DefaultRoleType, String> DEFAULT_ROLE_NAMES = Map.of(
			DefaultRoleType.OWNER, "Admin / Owner",
			DefaultRoleType.ACCOUNTING, "Accounting",
			DefaultRoleType.TEACHER, "Teacher",
			DefaultRoleType.PARENT, "Parent",
			DefaultRoleType.STUDENT, "Student");

	private final RoleRepository roleRepository;
	private final TenantUserRepository tenantUserRepository;
	private final TenantAuthorizationService tenantAuthorizationService;

	@Override
	public List<RoleResponse> list(UUID userId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		seedDefaultsIfMissing(tenantId);

		return roleRepository.findByTenantIdOrderByNameAsc(tenantId).stream()
				.map(role -> RoleMapper.toResponse(role, tenantUserRepository.countByRoleId(role.getId())))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RoleResponse getById(UUID userId, UUID id) {
		Role role = findInTenantOrThrow(userId, id);
		return RoleMapper.toResponse(role, tenantUserRepository.countByRoleId(role.getId()));
	}

	@Override
	public RoleResponse create(UUID userId, RoleRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		ensureNameIsAvailable(tenantId, null, request.name());

		Role role = Role.builder()
				.tenantId(tenantId)
				.name(request.name().trim())
				.defaultRole(false)
				.permissions(RoleMapper.toPermissions(request.permissions()))
				.branchIds(request.branchIds() == null ? new ArrayList<>() : new ArrayList<>(request.branchIds()))
				.build();

		return RoleMapper.toResponse(roleRepository.saveAndFlush(role), 0L);
	}

	@Override
	public RoleResponse update(UUID userId, UUID id, RoleRequest request) {
		Role role = findInTenantOrThrow(userId, id);
		rejectDefaultRoleChange(role);
		ensureNameIsAvailable(role.getTenantId(), id, request.name());

		role.setName(request.name().trim());
		// Replaced rather than merged: the grid the user submitted is the grid they
		// meant, and a merge would make unticking a box impossible.
		role.setPermissions(RoleMapper.toPermissions(request.permissions()));
		role.setBranchIds(request.branchIds() == null ? new ArrayList<>() : new ArrayList<>(request.branchIds()));

		return RoleMapper.toResponse(roleRepository.saveAndFlush(role),
				tenantUserRepository.countByRoleId(role.getId()));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		Role role = findInTenantOrThrow(userId, id);
		rejectDefaultRoleChange(role);

		long holders = tenantUserRepository.countByRoleId(id);
		if (holders > 0) {
			throw new ApiException(HttpStatus.CONFLICT, "ROLE_IN_USE",
					"This role is still held by " + holders + " user(s). Move them to another role first.");
		}

		roleRepository.delete(role);
	}

	private void seedDefaultsIfMissing(UUID tenantId) {
		if (roleRepository.existsByTenantId(tenantId)) {
			return;
		}

		List<Role> defaults = DEFAULT_ROLES.entrySet().stream()
				.map(entry -> Role.builder()
						.tenantId(tenantId)
						.name(DEFAULT_ROLE_NAMES.get(entry.getKey()))
						.defaultRole(true)
						.defaultType(entry.getKey())
						.permissions(everyActionOn(entry.getValue()))
						.branchIds(new ArrayList<>())
						.build())
				.toList();

		roleRepository.saveAll(defaults);
		roleRepository.flush();
	}

	private java.util.Set<RolePermission> everyActionOn(List<String> modules) {
		java.util.Set<RolePermission> permissions = new java.util.LinkedHashSet<>();
		for (String module : modules) {
			for (PermissionAction action : ALL_ACTIONS) {
				permissions.add(new RolePermission(module, action));
			}
		}
		return permissions;
	}

	/** 64-users-and-roles.md: the seeded roles are fixed. */
	private void rejectDefaultRoleChange(Role role) {
		if (role.isDefaultRole()) {
			throw new ApiException(HttpStatus.CONFLICT, "DEFAULT_ROLE_IMMUTABLE",
					"Default roles cannot be edited or deleted.");
		}
	}

	private void ensureNameIsAvailable(UUID tenantId, UUID roleId, String name) {
		roleRepository.findByTenantIdAndNameIgnoreCase(tenantId, name.trim())
				.filter(existing -> !existing.getId().equals(roleId))
				.ifPresent(existing -> {
					throw new DuplicateResourceException("A role named '" + name.trim() + "' already exists");
				});
	}

	private Role findInTenantOrThrow(UUID userId, UUID id) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return roleRepository.findByIdAndTenantId(id, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
	}
}

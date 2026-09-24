package com.school_management_webapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.TenantUserInviteRequest;
import com.school_management_webapi.dto.request.TenantUserUpdateRequest;
import com.school_management_webapi.dto.response.TenantUserResponse;
import com.school_management_webapi.entity.Role;
import com.school_management_webapi.entity.Tenant;
import com.school_management_webapi.entity.TenantUser;
import com.school_management_webapi.entity.TenantUserRole;
import com.school_management_webapi.entity.User;
import com.school_management_webapi.entity.UserStatus;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.repository.RoleRepository;
import com.school_management_webapi.repository.TenantRepository;
import com.school_management_webapi.repository.TenantUserRepository;
import com.school_management_webapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Who may reach the tenant, and as what.
 *
 * An invitation creates the account without a usable password: the person sets
 * one through the existing forgot-password flow, so no credential is ever put
 * in an email or handed back through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TenantUserServiceImpl implements TenantUserService {

	private final TenantUserRepository tenantUserRepository;
	private final TenantRepository tenantRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final TenantAuthorizationService tenantAuthorizationService;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional(readOnly = true)
	public List<TenantUserResponse> list(UUID userId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return tenantUserRepository.findByTenantIdOrderByCreatedAtAsc(tenantId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	public TenantUserResponse invite(UUID userId, TenantUserInviteRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		Role role = requireRoleInTenant(request.roleId(), tenantId);
		String email = request.email().trim().toLowerCase();

		User account = userRepository.findByEmail(email).orElseGet(() -> createPendingAccount(request, email));

		if (tenantUserRepository.existsByTenantIdAndUserId(tenantId, account.getId())) {
			throw new DuplicateResourceException("That email already has access to this school");
		}

		Tenant tenant = tenantRepository.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

		TenantUser membership = TenantUser.builder()
				.tenant(tenant)
				.user(account)
				.role(TenantUserRole.MEMBER)
				.roleId(role.getId())
				.branchIds(request.branchIds() == null ? new ArrayList<>() : new ArrayList<>(request.branchIds()))
				.build();

		return toResponse(tenantUserRepository.saveAndFlush(membership));
	}

	@Override
	public TenantUserResponse update(UUID userId, UUID targetUserId, TenantUserUpdateRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		TenantUser membership = requireMembership(tenantId, targetUserId);
		Role role = requireRoleInTenant(request.roleId(), tenantId);

		rejectSelfLockout(userId, targetUserId, request.status());

		membership.setRoleId(role.getId());
		membership.setBranchIds(
				request.branchIds() == null ? new ArrayList<>() : new ArrayList<>(request.branchIds()));

		User account = membership.getUser();
		account.setName(request.fullName().trim());
		account.setStatus(request.status());
		userRepository.saveAndFlush(account);

		return toResponse(tenantUserRepository.saveAndFlush(membership));
	}

	@Override
	public void remove(UUID userId, UUID targetUserId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		TenantUser membership = requireMembership(tenantId, targetUserId);

		if (userId.equals(targetUserId)) {
			throw new ApiException(HttpStatus.CONFLICT, "CANNOT_REMOVE_SELF",
					"You cannot remove your own access.");
		}
		if (membership.getRole() == TenantUserRole.OWNER) {
			throw new ApiException(HttpStatus.CONFLICT, "CANNOT_REMOVE_OWNER",
					"The owner of a school cannot be removed.");
		}

		// Only the membership goes: the account may belong to other tenants, and
		// deleting it here would take their access with it.
		tenantUserRepository.delete(membership);
	}

	/**
	 * The password is random and thrown away rather than left empty, so the row
	 * can never be logged into until the person sets one for themselves.
	 */
	private User createPendingAccount(TenantUserInviteRequest request, String email) {
		return userRepository.saveAndFlush(User.builder()
				.name(request.fullName().trim())
				.email(email)
				.passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
				.status(UserStatus.ACTIVE)
				.build());
	}

	/** Losing your own way back in is not something a settings screen should allow. */
	private void rejectSelfLockout(UUID userId, UUID targetUserId, UserStatus status) {
		if (userId.equals(targetUserId) && status != UserStatus.ACTIVE) {
			throw new ApiException(HttpStatus.CONFLICT, "CANNOT_DEACTIVATE_SELF",
					"You cannot deactivate your own account.");
		}
	}

	private Role requireRoleInTenant(UUID roleId, UUID tenantId) {
		return roleRepository.findByIdAndTenantId(roleId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", "Role not found."));
	}

	private TenantUser requireMembership(UUID tenantId, UUID targetUserId) {
		return tenantUserRepository.findByTenantIdAndUserId(tenantId, targetUserId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));
	}

	private TenantUserResponse toResponse(TenantUser membership) {
		User account = membership.getUser();
		String roleName = membership.getRoleId() == null ? null
				: roleRepository.findById(membership.getRoleId()).map(Role::getName).orElse(null);

		return new TenantUserResponse(
				membership.getId(),
				account.getId(),
				account.getName(),
				account.getEmail(),
				membership.getRoleId(),
				roleName,
				membership.getRole(),
				List.copyOf(membership.getBranchIds()),
				account.getStatus(),
				account.getLastLoginAt(),
				membership.getCreatedAt());
	}
}

package com.school_management_webapi.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.TenantUserInviteRequest;
import com.school_management_webapi.dto.request.TenantUserUpdateRequest;
import com.school_management_webapi.dto.response.TenantUserResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.TenantUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class TenantUserController {

	private final TenantUserService tenantUserService;

	@GetMapping
	@Operation(summary = "List everyone with access to the tenant")
	public ResponseEntity<List<TenantUserResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(tenantUserService.list(principal.getUser().getId()));
	}

	@PostMapping
	@Operation(summary = "Invite someone to the tenant. They set their own password afterwards")
	public ResponseEntity<TenantUserResponse> invite(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody TenantUserInviteRequest request) {
		TenantUserResponse created = tenantUserService.invite(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/users/" + created.userId())).body(created);
	}

	@PutMapping("/{userId}")
	@Operation(summary = "Change someone's name, role, branches or status")
	public ResponseEntity<TenantUserResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID userId, @Valid @RequestBody TenantUserUpdateRequest request) {
		return ResponseEntity.ok(tenantUserService.update(principal.getUser().getId(), userId, request));
	}

	@DeleteMapping("/{userId}")
	@Operation(summary = "Remove someone's access to the tenant, leaving their account intact")
	public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID userId) {
		tenantUserService.remove(principal.getUser().getId(), userId);
		return ResponseEntity.noContent().build();
	}
}

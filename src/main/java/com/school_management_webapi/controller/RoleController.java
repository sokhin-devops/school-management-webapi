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

import com.school_management_webapi.dto.request.RoleRequest;
import com.school_management_webapi.dto.response.RoleResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles")
public class RoleController {

	private final RoleService roleService;

	@GetMapping
	@Operation(summary = "List the roles of the tenant, seeding the five defaults on first use")
	public ResponseEntity<List<RoleResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(roleService.list(principal.getUser().getId()));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a role by id")
	public ResponseEntity<RoleResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(roleService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a custom role")
	public ResponseEntity<RoleResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody RoleRequest request) {
		RoleResponse created = roleService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/roles/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a custom role. Default roles cannot be edited")
	public ResponseEntity<RoleResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
		return ResponseEntity.ok(roleService.update(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a custom role that nobody holds. Default roles cannot be deleted")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		roleService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

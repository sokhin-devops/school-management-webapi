package com.school_management_webapi.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.ParentCreateRequest;
import com.school_management_webapi.dto.request.ParentPatchRequest;
import com.school_management_webapi.dto.request.ParentUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.ParentResponse;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.ParentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
@Tag(name = "Parents")
public class ParentController {

	private final ParentService parentService;

	@GetMapping
	@Operation(summary = "List parents of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<ParentResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) RecordStatus status,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(parentService.list(principal.getUser().getId(), branchId, status, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a parent by id")
	public ResponseEntity<ParentResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(parentService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a parent")
	public ResponseEntity<ParentResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody ParentCreateRequest request) {
		ParentResponse created = parentService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/parents/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a parent")
	public ResponseEntity<ParentResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody ParentUpdateRequest request) {
		return ResponseEntity.ok(parentService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a parent")
	public ResponseEntity<ParentResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody ParentPatchRequest request) {
		return ResponseEntity.ok(parentService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a parent")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		parentService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

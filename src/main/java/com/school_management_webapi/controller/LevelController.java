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

import com.school_management_webapi.dto.request.LevelCreateRequest;
import com.school_management_webapi.dto.request.LevelPatchRequest;
import com.school_management_webapi.dto.request.LevelUpdateRequest;
import com.school_management_webapi.dto.response.LevelResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.LevelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/levels")
@RequiredArgsConstructor
@Tag(name = "Levels")
public class LevelController {

	private final LevelService levelService;

	@GetMapping
	@Operation(summary = "List levels of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<LevelResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) RecordStatus status,
			@RequestParam(required = false) UUID programId,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(levelService.list(principal.getUser().getId(), branchId, status, programId, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a level by id")
	public ResponseEntity<LevelResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(levelService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a level")
	public ResponseEntity<LevelResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody LevelCreateRequest request) {
		LevelResponse created = levelService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/levels/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a level")
	public ResponseEntity<LevelResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody LevelUpdateRequest request) {
		return ResponseEntity.ok(levelService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a level")
	public ResponseEntity<LevelResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody LevelPatchRequest request) {
		return ResponseEntity.ok(levelService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a level")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		levelService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

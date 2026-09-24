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

import com.school_management_webapi.dto.request.ProgramCreateRequest;
import com.school_management_webapi.dto.request.ProgramPatchRequest;
import com.school_management_webapi.dto.request.ProgramUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.ProgramResponse;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.ProgramService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
@Tag(name = "Programs")
public class ProgramController {

	private final ProgramService programService;

	@GetMapping
	@Operation(summary = "List programmes of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<ProgramResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) RecordStatus status,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(programService.list(principal.getUser().getId(), branchId, status, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a programme by id")
	public ResponseEntity<ProgramResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(programService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a programme")
	public ResponseEntity<ProgramResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody ProgramCreateRequest request) {
		ProgramResponse created = programService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/programs/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a programme")
	public ResponseEntity<ProgramResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody ProgramUpdateRequest request) {
		return ResponseEntity.ok(programService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a programme")
	public ResponseEntity<ProgramResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody ProgramPatchRequest request) {
		return ResponseEntity.ok(programService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a programme")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		programService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

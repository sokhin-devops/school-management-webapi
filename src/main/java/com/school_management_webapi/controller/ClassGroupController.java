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

import com.school_management_webapi.dto.request.ClassGroupCreateRequest;
import com.school_management_webapi.dto.request.ClassGroupPatchRequest;
import com.school_management_webapi.dto.request.ClassGroupUpdateRequest;
import com.school_management_webapi.dto.response.ClassGroupResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.ClassGroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Classes")
public class ClassGroupController {

	private final ClassGroupService classGroupService;

	@GetMapping
	@Operation(summary = "List classes of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<ClassGroupResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) RecordStatus status,
			@RequestParam(required = false) UUID academicYearId,
			@RequestParam(required = false) UUID levelId,
			@RequestParam(required = false) UUID programId,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(classGroupService.list(principal.getUser().getId(), branchId, status, academicYearId, levelId, programId, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a class by id")
	public ResponseEntity<ClassGroupResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(classGroupService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a class")
	public ResponseEntity<ClassGroupResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody ClassGroupCreateRequest request) {
		ClassGroupResponse created = classGroupService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/classes/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a class")
	public ResponseEntity<ClassGroupResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody ClassGroupUpdateRequest request) {
		return ResponseEntity.ok(classGroupService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a class")
	public ResponseEntity<ClassGroupResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody ClassGroupPatchRequest request) {
		return ResponseEntity.ok(classGroupService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a class")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		classGroupService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

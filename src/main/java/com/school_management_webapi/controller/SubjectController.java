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

import com.school_management_webapi.dto.request.SubjectCreateRequest;
import com.school_management_webapi.dto.request.SubjectPatchRequest;
import com.school_management_webapi.dto.request.SubjectUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.SubjectResponse;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.SubjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects")
public class SubjectController {

	private final SubjectService subjectService;

	@GetMapping
	@Operation(summary = "List subjects of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<SubjectResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) RecordStatus status,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(subjectService.list(principal.getUser().getId(), branchId, status, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a subject by id")
	public ResponseEntity<SubjectResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(subjectService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a subject")
	public ResponseEntity<SubjectResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody SubjectCreateRequest request) {
		SubjectResponse created = subjectService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/subjects/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a subject")
	public ResponseEntity<SubjectResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody SubjectUpdateRequest request) {
		return ResponseEntity.ok(subjectService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a subject")
	public ResponseEntity<SubjectResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody SubjectPatchRequest request) {
		return ResponseEntity.ok(subjectService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a subject")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		subjectService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

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

import com.school_management_webapi.dto.request.TeacherCreateRequest;
import com.school_management_webapi.dto.request.TeacherPatchRequest;
import com.school_management_webapi.dto.request.TeacherUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.TeacherResponse;
import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.TeacherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers")
public class TeacherController {

	private final TeacherService teacherService;

	@GetMapping
	@Operation(summary = "List teachers of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<TeacherResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) RecordStatus status,
			@RequestParam(required = false) Gender gender,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(teacherService.list(principal.getUser().getId(), branchId, status, gender, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a teacher by id")
	public ResponseEntity<TeacherResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(teacherService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a teacher")
	public ResponseEntity<TeacherResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody TeacherCreateRequest request) {
		TeacherResponse created = teacherService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/teachers/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a teacher")
	public ResponseEntity<TeacherResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody TeacherUpdateRequest request) {
		return ResponseEntity.ok(teacherService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a teacher")
	public ResponseEntity<TeacherResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody TeacherPatchRequest request) {
		return ResponseEntity.ok(teacherService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a teacher")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		teacherService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

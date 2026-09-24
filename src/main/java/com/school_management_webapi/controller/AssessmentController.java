package com.school_management_webapi.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.school_management_webapi.dto.request.AssessmentCreateRequest;
import com.school_management_webapi.dto.request.AssessmentPatchRequest;
import com.school_management_webapi.dto.request.AssessmentUpdateRequest;
import com.school_management_webapi.dto.response.AssessmentResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.AssessmentType;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.AssessmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/assessments")
@RequiredArgsConstructor
@Tag(name = "Assessments")
public class AssessmentController {

	private final AssessmentService assessmentService;

	@GetMapping
	@Operation(summary = "List assessments of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<AssessmentResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) AssessmentType type,
			@RequestParam(required = false) UUID classGroupId,
			@RequestParam(required = false) UUID subjectId,
			@RequestParam(required = false) UUID academicYearId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assessedOnFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assessedOnTo,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(assessmentService.list(principal.getUser().getId(), branchId, type, classGroupId, subjectId, academicYearId, assessedOnFrom, assessedOnTo, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get an assessment by id")
	public ResponseEntity<AssessmentResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(assessmentService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create an assessment")
	public ResponseEntity<AssessmentResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody AssessmentCreateRequest request) {
		AssessmentResponse created = assessmentService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/assessments/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace an assessment")
	public ResponseEntity<AssessmentResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody AssessmentUpdateRequest request) {
		return ResponseEntity.ok(assessmentService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update an assessment")
	public ResponseEntity<AssessmentResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody AssessmentPatchRequest request) {
		return ResponseEntity.ok(assessmentService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) an assessment")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		assessmentService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

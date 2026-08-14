package com.school_management_webapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.AcademicYearRequest;
import com.school_management_webapi.dto.response.AcademicYearResponse;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.AcademicYearService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/academic-years")
@RequiredArgsConstructor
@Tag(name = "Academic Years")
public class AcademicYearController {

	private final AcademicYearService academicYearService;

	@PostMapping
	@Operation(summary = "Create an academic year under a school")
	public ResponseEntity<ApiResponse<AcademicYearResponse>> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody AcademicYearRequest request) {
		AcademicYearResponse response = academicYearService.create(principal.getUser().getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Academic year created successfully", response));
	}

	@GetMapping
	@Operation(summary = "List academic years, optionally filtered by school")
	public ResponseEntity<ApiResponse<List<AcademicYearResponse>>> list(
			@AuthenticationPrincipal UserPrincipal principal, @RequestParam(required = false) UUID schoolId) {
		return ResponseEntity.ok(ApiResponse.success("Academic years retrieved successfully",
				academicYearService.list(principal.getUser().getId(), schoolId)));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get an academic year by id")
	public ResponseEntity<ApiResponse<AcademicYearResponse>> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success("Academic year retrieved successfully",
				academicYearService.getById(principal.getUser().getId(), id)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update an academic year")
	public ResponseEntity<ApiResponse<AcademicYearResponse>> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody AcademicYearRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Academic year updated successfully",
				academicYearService.update(principal.getUser().getId(), id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) an academic year")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		academicYearService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

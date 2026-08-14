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
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.SchoolRequest;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.SchoolResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.SchoolService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
@Tag(name = "Schools")
public class SchoolController {

	private final SchoolService schoolService;

	@PostMapping
	@Operation(summary = "Create a school for the authenticated tenant")
	public ResponseEntity<ApiResponse<SchoolResponse>> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody SchoolRequest request) {
		SchoolResponse response = schoolService.create(principal.getUser().getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("School created successfully", response));
	}

	@GetMapping
	@Operation(summary = "List the tenant's schools")
	public ResponseEntity<ApiResponse<List<SchoolResponse>>> list(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Schools retrieved successfully",
				schoolService.list(principal.getUser().getId())));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a school by id")
	public ResponseEntity<ApiResponse<SchoolResponse>> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success("School retrieved successfully",
				schoolService.getById(principal.getUser().getId(), id)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a school")
	public ResponseEntity<ApiResponse<SchoolResponse>> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody SchoolRequest request) {
		return ResponseEntity.ok(ApiResponse.success("School updated successfully",
				schoolService.update(principal.getUser().getId(), id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a school")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		schoolService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

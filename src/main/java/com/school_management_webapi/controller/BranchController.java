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

import com.school_management_webapi.dto.request.BranchRequest;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.BranchResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.BranchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branches")
public class BranchController {

	private final BranchService branchService;

	@PostMapping
	@Operation(summary = "Create a branch under a school")
	public ResponseEntity<ApiResponse<BranchResponse>> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody BranchRequest request) {
		BranchResponse response = branchService.create(principal.getUser().getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Branch created successfully", response));
	}

	@GetMapping
	@Operation(summary = "List branches, optionally filtered by school")
	public ResponseEntity<ApiResponse<List<BranchResponse>>> list(@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID schoolId) {
		return ResponseEntity.ok(ApiResponse.success("Branches retrieved successfully",
				branchService.list(principal.getUser().getId(), schoolId)));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a branch by id")
	public ResponseEntity<ApiResponse<BranchResponse>> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success("Branch retrieved successfully",
				branchService.getById(principal.getUser().getId(), id)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a branch")
	public ResponseEntity<ApiResponse<BranchResponse>> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody BranchRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Branch updated successfully",
				branchService.update(principal.getUser().getId(), id, request)));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a branch")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		branchService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

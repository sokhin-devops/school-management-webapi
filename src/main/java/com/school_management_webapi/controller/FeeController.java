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

import com.school_management_webapi.dto.request.FeeCreateRequest;
import com.school_management_webapi.dto.request.FeePatchRequest;
import com.school_management_webapi.dto.request.FeeUpdateRequest;
import com.school_management_webapi.dto.response.FeeResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.FeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/fees")
@RequiredArgsConstructor
@Tag(name = "Fees")
public class FeeController {

	private final FeeService feeService;

	@GetMapping
	@Operation(summary = "List fees of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<FeeResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) RecordStatus status,
			@RequestParam(required = false) UUID academicYearId,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(feeService.list(principal.getUser().getId(), branchId, status, academicYearId, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a fee by id")
	public ResponseEntity<FeeResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(feeService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a fee")
	public ResponseEntity<FeeResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody FeeCreateRequest request) {
		FeeResponse created = feeService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/fees/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a fee")
	public ResponseEntity<FeeResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody FeeUpdateRequest request) {
		return ResponseEntity.ok(feeService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a fee")
	public ResponseEntity<FeeResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody FeePatchRequest request) {
		return ResponseEntity.ok(feeService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a fee")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		feeService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

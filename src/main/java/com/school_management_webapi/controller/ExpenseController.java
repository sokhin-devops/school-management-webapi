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

import com.school_management_webapi.dto.request.ExpenseCreateRequest;
import com.school_management_webapi.dto.request.ExpensePatchRequest;
import com.school_management_webapi.dto.request.ExpenseUpdateRequest;
import com.school_management_webapi.dto.response.ExpenseResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.ExpenseStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.ExpenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses")
public class ExpenseController {

	private final ExpenseService expenseService;

	@GetMapping
	@Operation(summary = "List expenses of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<ExpenseResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) ExpenseStatus status,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(expenseService.list(principal.getUser().getId(), branchId, status, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get an expense by id")
	public ResponseEntity<ExpenseResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(expenseService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create an expense")
	public ResponseEntity<ExpenseResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody ExpenseCreateRequest request) {
		ExpenseResponse created = expenseService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/expenses/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace an expense")
	public ResponseEntity<ExpenseResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody ExpenseUpdateRequest request) {
		return ResponseEntity.ok(expenseService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update an expense")
	public ResponseEntity<ExpenseResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody ExpensePatchRequest request) {
		return ResponseEntity.ok(expenseService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) an expense")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		expenseService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

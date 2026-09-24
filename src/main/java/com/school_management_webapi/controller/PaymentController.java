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

import com.school_management_webapi.dto.request.PaymentCreateRequest;
import com.school_management_webapi.dto.request.PaymentPatchRequest;
import com.school_management_webapi.dto.request.PaymentUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.PaymentResponse;
import com.school_management_webapi.entity.PaymentMethod;
import com.school_management_webapi.entity.PaymentStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

	private final PaymentService paymentService;

	@GetMapping
	@Operation(summary = "List payments of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<PaymentResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) PaymentStatus status,
			@RequestParam(required = false) PaymentMethod method,
			@RequestParam(required = false) UUID feeId,
			@RequestParam(required = false) UUID studentId,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(paymentService.list(principal.getUser().getId(), branchId, status, method, feeId, studentId, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a payment by id")
	public ResponseEntity<PaymentResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(paymentService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a payment")
	public ResponseEntity<PaymentResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody PaymentCreateRequest request) {
		PaymentResponse created = paymentService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/payments/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a payment")
	public ResponseEntity<PaymentResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody PaymentUpdateRequest request) {
		return ResponseEntity.ok(paymentService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a payment")
	public ResponseEntity<PaymentResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody PaymentPatchRequest request) {
		return ResponseEntity.ok(paymentService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a payment")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		paymentService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

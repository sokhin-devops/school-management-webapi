package com.school_management_webapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.CancelSubscriptionRequest;
import com.school_management_webapi.dto.request.PlanSelectionRequest;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.SubscriptionCancelResponse;
import com.school_management_webapi.dto.response.SubscriptionResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions")
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	@PostMapping
	@Operation(summary = "Select a plan and create a subscription for the tenant")
	public ResponseEntity<ApiResponse<SubscriptionResponse>> select(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody PlanSelectionRequest request) {
		SubscriptionResponse response = subscriptionService.selectPlan(principal.getUser().getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Subscription created successfully.", response));
	}

	@GetMapping("/current")
	@Operation(summary = "Get the tenant's current subscription")
	public ResponseEntity<ApiResponse<SubscriptionResponse>> current(@AuthenticationPrincipal UserPrincipal principal) {
		SubscriptionResponse response = subscriptionService.getCurrent(principal.getUser().getId());
		return ResponseEntity.ok(ApiResponse.success("Current subscription retrieved", response));
	}

	@GetMapping
	@Operation(summary = "Get the tenant's subscription history")
	public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> history(
			@AuthenticationPrincipal UserPrincipal principal) {
		List<SubscriptionResponse> response = subscriptionService.getHistory(principal.getUser().getId());
		return ResponseEntity.ok(ApiResponse.success("Subscription history retrieved", response));
	}

	@PatchMapping("/current")
	@Operation(summary = "Change the tenant's plan or billing cycle")
	public ResponseEntity<ApiResponse<SubscriptionResponse>> changePlan(
			@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PlanSelectionRequest request) {
		SubscriptionResponse response = subscriptionService.changePlan(principal.getUser().getId(), request);
		return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully.", response));
	}

	@PostMapping("/current/cancel")
	@Operation(summary = "Cancel the tenant's current subscription")
	public ResponseEntity<ApiResponse<SubscriptionCancelResponse>> cancel(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestBody(required = false) CancelSubscriptionRequest request) {
		CancelSubscriptionRequest body = request != null ? request : new CancelSubscriptionRequest(null);
		SubscriptionCancelResponse response = subscriptionService.cancel(principal.getUser().getId(), body);
		return ResponseEntity.ok(ApiResponse.success("Subscription cancellation scheduled successfully.", response));
	}
}

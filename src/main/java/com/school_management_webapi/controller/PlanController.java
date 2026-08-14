package com.school_management_webapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.PlanResponse;
import com.school_management_webapi.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Plans")
public class PlanController {

	private final PlanService planService;

	@GetMapping
	@Operation(summary = "List active subscription plans")
	public ResponseEntity<ApiResponse<List<PlanResponse>>> list() {
		return ResponseEntity.ok(ApiResponse.success("Plans retrieved successfully", planService.listActivePlans()));
	}

	@GetMapping("/{planId}")
	@Operation(summary = "Get plan details by id")
	public ResponseEntity<ApiResponse<PlanResponse>> getById(@PathVariable UUID planId) {
		return ResponseEntity.ok(ApiResponse.success("Plan details retrieved successfully",
				planService.getPlanById(planId)));
	}
}

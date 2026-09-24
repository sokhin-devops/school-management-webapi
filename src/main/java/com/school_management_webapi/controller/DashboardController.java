package com.school_management_webapi.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.response.DashboardSummaryResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping("/summary")
	@Operation(summary = "Counts, today's attendance and the money trend for one branch or the whole tenant")
	public ResponseEntity<DashboardSummaryResponse> summary(@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId) {
		return ResponseEntity.ok(dashboardService.summary(principal.getUser().getId(), branchId));
	}
}

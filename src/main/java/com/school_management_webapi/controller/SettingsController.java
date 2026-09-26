package com.school_management_webapi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.SettingsRequest;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.SettingsResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.AuditService;
import com.school_management_webapi.service.TenantAuthorizationService;
import com.school_management_webapi.service.TenantSettingsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 60-settings.md's sections that are settings rather than records. Every write
 * here is a Settings edit in the role grid; reading the academic section is
 * open to everyone signed in, because it decides what their menu shows.
 */
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings")
public class SettingsController {

	private final TenantSettingsService settingsService;
	private final AuditService auditService;
	private final TenantAuthorizationService tenantAuthorizationService;

	@GetMapping("/security")
	@Operation(summary = "The school's password and session rules")
	public ResponseEntity<ApiResponse<SettingsResponse.Security>> security(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Security settings retrieved",
				settingsService.getSecurity(principal.getUser().getId())));
	}

	@PutMapping("/security")
	@Operation(summary = "Change the school's password and session rules")
	public ResponseEntity<ApiResponse<SettingsResponse.Security>> updateSecurity(
			@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody SettingsRequest.Security request) {
		return ResponseEntity.ok(ApiResponse.success("Security settings saved",
				settingsService.updateSecurity(principal.getUser().getId(), request)));
	}

	@GetMapping("/security/audit")
	@Operation(summary = "Recent security events across the school, newest first")
	public ResponseEntity<ApiResponse<List<SettingsResponse.AuditEntry>>> audit(
			@AuthenticationPrincipal UserPrincipal principal, @RequestParam(defaultValue = "50") int size) {
		var tenantId = tenantAuthorizationService.requireTenantId(principal.getUser().getId());
		return ResponseEntity.ok(ApiResponse.success("Audit log retrieved", auditService.recent(tenantId, size)));
	}

	@GetMapping("/academic")
	@Operation(summary = "Which academic concepts the school uses, and what it calls them")
	public ResponseEntity<ApiResponse<SettingsResponse.Academic>> academic(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Academic settings retrieved",
				settingsService.getAcademic(principal.getUser().getId())));
	}

	@PutMapping("/academic")
	@Operation(summary = "Change the school's academic concepts, terms and grading")
	public ResponseEntity<ApiResponse<SettingsResponse.Academic>> updateAcademic(
			@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody SettingsRequest.Academic request) {
		return ResponseEntity.ok(ApiResponse.success("Academic settings saved",
				settingsService.updateAcademic(principal.getUser().getId(), request)));
	}

	@GetMapping("/system")
	@Operation(summary = "Platform-level switches for the school")
	public ResponseEntity<ApiResponse<SettingsResponse.SystemSettings>> system(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("System settings retrieved",
				settingsService.getSystem(principal.getUser().getId())));
	}

	@PutMapping("/system")
	@Operation(summary = "Turn maintenance mode on or off")
	public ResponseEntity<ApiResponse<SettingsResponse.SystemSettings>> updateSystem(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody SettingsRequest.SystemSettings request) {
		return ResponseEntity.ok(ApiResponse.success("System settings saved",
				settingsService.updateSystem(principal.getUser().getId(), request)));
	}
}

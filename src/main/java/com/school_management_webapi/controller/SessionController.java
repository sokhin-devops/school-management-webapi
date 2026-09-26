package com.school_management_webapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.SettingsResponse;
import com.school_management_webapi.security.JwtAuthenticationFilter;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * The caller's own sign-ins. Under /auth rather than the role grid: everyone
 * may see and end their own sessions, whatever their role.
 */
@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class SessionController {

	private final SessionService sessionService;

	@GetMapping
	@Operation(summary = "Where the current user is signed in")
	public ResponseEntity<ApiResponse<List<SettingsResponse.Session>>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestAttribute(name = JwtAuthenticationFilter.SESSION_ATTRIBUTE, required = false) UUID current) {
		return ResponseEntity.ok(ApiResponse.success("Sessions retrieved",
				sessionService.list(principal.getUser().getId(), current)));
	}

	@DeleteMapping("/{sessionId}")
	@Operation(summary = "Sign one of the current user's sessions out")
	public ResponseEntity<Void> revoke(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID sessionId) {
		sessionService.revoke(principal.getUser().getId(), sessionId);
		return ResponseEntity.noContent().build();
	}
}

package com.school_management_webapi.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.TwoFactorRequest;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.TwoFactorResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.TwoFactorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 67-security.md: a person's own two-factor sign-in, under /auth so that it
 * stays reachable while their school requires it and they have not set it up;
 * and an admin's reset, under /users, which the Settings grid guards.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Two-factor sign-in")
public class TwoFactorController {

	private final TwoFactorService twoFactorService;

	@GetMapping("/auth/two-factor")
	@Operation(summary = "Whether the signed-in person uses two-factor, and whether their school requires it")
	public ResponseEntity<ApiResponse<TwoFactorResponse.Status>> status(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Two-factor status retrieved",
				twoFactorService.status(principal.getUser().getId())));
	}

	@PostMapping("/auth/two-factor/setup")
	@Operation(summary = "Start setup: a new secret for the authenticator app")
	public ResponseEntity<ApiResponse<TwoFactorResponse.Setup>> setup(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Scan the code with your authenticator app",
				twoFactorService.beginSetup(principal.getUser().getId())));
	}

	@PostMapping("/auth/two-factor/enable")
	@Operation(summary = "Finish setup with a code from the app; answers the recovery codes, once")
	public ResponseEntity<ApiResponse<TwoFactorResponse.RecoveryCodes>> enable(
			@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody TwoFactorRequest.Code request) {
		return ResponseEntity.ok(ApiResponse.success("Two-factor sign-in is on",
				twoFactorService.enable(principal.getUser().getId(), request.code())));
	}

	@PostMapping("/auth/two-factor/recovery-codes")
	@Operation(summary = "Replace the recovery codes with a new set")
	public ResponseEntity<ApiResponse<TwoFactorResponse.RecoveryCodes>> regenerate(
			@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody TwoFactorRequest.Code request) {
		return ResponseEntity.ok(ApiResponse.success("New recovery codes issued",
				twoFactorService.regenerateRecoveryCodes(principal.getUser().getId(), request.code())));
	}

	@PostMapping("/auth/two-factor/disable")
	@Operation(summary = "Turn two-factor sign-in off")
	public ResponseEntity<ApiResponse<Void>> disable(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody TwoFactorRequest.Disable request) {
		twoFactorService.disable(principal.getUser().getId(), request.password(), request.code());
		return ResponseEntity.ok(ApiResponse.success("Two-factor sign-in is off", null));
	}

	@DeleteMapping("/users/{userId}/two-factor")
	@Operation(summary = "Reset someone's two-factor sign-in, for a lost phone and lost recovery codes")
	public ResponseEntity<ApiResponse<Void>> reset(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID userId) {
		twoFactorService.resetFor(principal.getUser().getId(), userId);
		return ResponseEntity.ok(ApiResponse.success("Two-factor sign-in reset", null));
	}
}

package com.school_management_webapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.ForgotPasswordRequest;
import com.school_management_webapi.dto.request.LoginRequest;
import com.school_management_webapi.dto.request.TwoFactorRequest;
import com.school_management_webapi.dto.request.RefreshTokenRequest;
import com.school_management_webapi.dto.request.RegisterRequest;
import com.school_management_webapi.dto.request.ResetPasswordRequest;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.AuthResponse;
import com.school_management_webapi.dto.response.ForgotPasswordResponse;
import com.school_management_webapi.dto.response.MyPermissionsResponse;
import com.school_management_webapi.dto.response.UserResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.AuthService;
import com.school_management_webapi.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

	private final AuthService authService;
	private final PermissionService permissionService;

	@PostMapping("/register")
	@Operation(summary = "Register a new user account")
	public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
		AuthResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Registration successful", response));
	}

	@PostMapping("/login")
	@Operation(summary = "Authenticate a user and issue access/refresh tokens")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		AuthResponse response = authService.login(request);
		return ResponseEntity.ok(ApiResponse.success("Login successful", response));
	}

	@PostMapping("/login/two-factor")
	@Operation(summary = "Finish signing in with an authenticator or recovery code")
	public ResponseEntity<ApiResponse<AuthResponse>> loginWithTwoFactor(
			@Valid @RequestBody TwoFactorRequest.SignIn request) {
		AuthResponse response = authService.loginWithTwoFactor(request);
		return ResponseEntity.ok(ApiResponse.success("Login successful", response));
	}

	@PostMapping("/logout")
	@Operation(summary = "Revoke a refresh token")
	public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
		authService.logout(request.refreshToken());
		return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
	}

	@PostMapping("/refresh-token")
	@Operation(summary = "Exchange a valid refresh token for a new token pair")
	public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
		AuthResponse response = authService.refreshToken(request.refreshToken());
		return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
	}

	@PostMapping("/forgot-password")
	@Operation(summary = "Request a password reset token via email")
	public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
			@Valid @RequestBody ForgotPasswordRequest request) {
		ForgotPasswordResponse response = authService.forgotPassword(request);
		return ResponseEntity
				.ok(ApiResponse.success("If the email exists, a password reset link has been sent", response));
	}

	@PostMapping("/reset-password")
	@Operation(summary = "Reset a user's password using a valid reset token")
	public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
	}

	@GetMapping("/me")
	@Operation(summary = "Get the current authenticated user")
	public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
		UserResponse response = authService.getCurrentUser(principal.getUser().getId());
		return ResponseEntity.ok(ApiResponse.success("Current user retrieved", response));
	}

	@GetMapping("/me/permissions")
	@Operation(summary = "The permission grid the current user holds")
	public ResponseEntity<ApiResponse<MyPermissionsResponse>> getMyPermissions(
			@AuthenticationPrincipal UserPrincipal principal) {
		MyPermissionsResponse response = permissionService.permissionsFor(principal.getUser().getId());
		return ResponseEntity.ok(ApiResponse.success("Permissions retrieved", response));
	}
}

package com.school_management_webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 67-security.md: the bodies of the two-factor endpoints. */
public final class TwoFactorRequest {

	private TwoFactorRequest() {
	}

	/** A six-digit code from the app, or one of the recovery codes. */
	public record Code(
			@NotBlank(message = "code is required") @Size(max = 32, message = "code must be at most 32 characters") String code) {
	}

	/** The second step of signing in: the challenge the password earned, and a code. */
	public record SignIn(
			@NotBlank(message = "twoFactorToken is required") String twoFactorToken,
			@NotBlank(message = "code is required") @Size(max = 32, message = "code must be at most 32 characters") String code) {
	}

	/** Turning it off asks for both: the password alone is what two-factor guards against. */
	public record Disable(
			@NotBlank(message = "password is required") String password,
			@NotBlank(message = "code is required") @Size(max = 32, message = "code must be at most 32 characters") String code) {
	}
}

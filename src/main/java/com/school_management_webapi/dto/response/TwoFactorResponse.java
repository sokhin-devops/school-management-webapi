package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/** 67-security.md: what the two-factor endpoints answer. */
public final class TwoFactorResponse {

	private TwoFactorResponse() {
	}

	/** Where the signed-in person stands, and whether their school insists. */
	public record Status(
			boolean enabled,
			boolean required,
			LocalDateTime enabledAt,
			int recoveryCodesLeft) {
	}

	/** What the authenticator app needs: the QR code's contents, and the key to type by hand. */
	public record Setup(
			String secret,
			String otpauthUri) {
	}

	/** Shown once; only their hashes are kept. */
	public record RecoveryCodes(List<String> codes) {
	}
}

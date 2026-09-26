package com.school_management_webapi.dto.response;

/**
 * A sign-in. When the account uses two-factor, the password alone earns only
 * twoFactorToken - no tokens, no user - to be exchanged with a code at
 * /auth/login/two-factor.
 */
public record AuthResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresIn,
		UserResponse user,
		String twoFactorToken) {

	public static AuthResponse twoFactorChallenge(String twoFactorToken) {
		return new AuthResponse(null, null, null, 0, null, twoFactorToken);
	}
}

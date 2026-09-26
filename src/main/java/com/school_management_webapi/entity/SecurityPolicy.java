package com.school_management_webapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 67-security.md: the rules a school's passwords and sessions follow.
 *
 * Every field is enforced somewhere - the policy on password reset, the expiry
 * at sign-in, the timeout on token refresh. A rule the server did not act on
 * would be a promise the Settings screen made on its behalf.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityPolicy {

	/** The platform's own floor is 8; a school may ask for more, not less. */
	public static final int MIN_LENGTH_FLOOR = 8;

	@Column(name = "password_min_length", nullable = false)
	@Builder.Default
	private int passwordMinLength = MIN_LENGTH_FLOOR;

	@Column(name = "password_require_uppercase", nullable = false)
	@Builder.Default
	private boolean passwordRequireUppercase = true;

	@Column(name = "password_require_number", nullable = false)
	@Builder.Default
	private boolean passwordRequireNumber = true;

	@Column(name = "password_require_symbol", nullable = false)
	@Builder.Default
	private boolean passwordRequireSymbol = false;

	/** 0 means passwords never expire. */
	@Column(name = "password_expiry_days", nullable = false)
	@Builder.Default
	private int passwordExpiryDays = 0;

	@Column(name = "sign_out_on_password_change", nullable = false)
	@Builder.Default
	private boolean signOutOnPasswordChange = true;

	/** How long a session may sit unused before it has to sign in again. */
	@Column(name = "session_timeout_minutes", nullable = false)
	@Builder.Default
	private int sessionTimeoutMinutes = 480;

	@Column(name = "two_factor_required", nullable = false)
	@Builder.Default
	private boolean twoFactorRequired = false;

	public static SecurityPolicy defaults() {
		return SecurityPolicy.builder().build();
	}
}

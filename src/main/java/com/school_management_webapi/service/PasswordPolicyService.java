package com.school_management_webapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.school_management_webapi.entity.SecurityPolicy;
import com.school_management_webapi.entity.User;
import com.school_management_webapi.exception.ApiException;

/** Applies a school's password rules - 67-security.md. */
@Service
public class PasswordPolicyService {

	/**
	 * Refuses a password that breaks the policy, naming every rule it breaks at
	 * once, so a person fixes it in one attempt rather than one rule at a time.
	 */
	public void requireAcceptable(SecurityPolicy policy, String password) {
		List<String> unmet = new ArrayList<>();
		if (password == null || password.length() < policy.getPasswordMinLength()) {
			unmet.add("at least " + policy.getPasswordMinLength() + " characters");
		}
		String value = password == null ? "" : password;
		if (policy.isPasswordRequireUppercase() && value.chars().noneMatch(Character::isUpperCase)) {
			unmet.add("an uppercase letter");
		}
		if (policy.isPasswordRequireNumber() && value.chars().noneMatch(Character::isDigit)) {
			unmet.add("a number");
		}
		if (policy.isPasswordRequireSymbol() && value.chars().allMatch(Character::isLetterOrDigit)) {
			unmet.add("a symbol");
		}
		if (!unmet.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_POLICY",
					"Your school requires passwords with " + String.join(", ", unmet) + ".");
		}
	}

	/** Counted from the last change, or from the account's creation if it has never changed. */
	public boolean isExpired(SecurityPolicy policy, User user) {
		if (policy.getPasswordExpiryDays() <= 0) {
			return false;
		}
		LocalDateTime since = user.getPasswordChangedAt() != null ? user.getPasswordChangedAt() : user.getCreatedAt();
		return since != null && since.plusDays(policy.getPasswordExpiryDays()).isBefore(LocalDateTime.now());
	}
}

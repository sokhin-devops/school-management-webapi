package com.school_management_webapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.school_management_webapi.entity.SecurityPolicy;
import com.school_management_webapi.entity.User;
import com.school_management_webapi.exception.ApiException;

/** 67-security.md: a school's password rules, and when a password expires. */
class PasswordPolicyServiceTest {

	private final PasswordPolicyService service = new PasswordPolicyService();

	@Test
	void acceptsAPasswordThatMeetsEveryRule() {
		SecurityPolicy policy = SecurityPolicy.builder().passwordMinLength(10).passwordRequireSymbol(true).build();
		assertThatCode(() -> service.requireAcceptable(policy, "Password12!")).doesNotThrowAnyException();
	}

	@Test
	void namesEveryBrokenRuleAtOnce() {
		SecurityPolicy policy = SecurityPolicy.builder().passwordMinLength(12).passwordRequireSymbol(true).build();
		assertThatThrownBy(() -> service.requireAcceptable(policy, "password"))
				.isInstanceOf(ApiException.class)
				.hasMessage("Your school requires passwords with at least 12 characters, an uppercase letter, "
						+ "a number, a symbol.");
	}

	@Test
	void rulesASchoolTurnsOffAreNotApplied() {
		SecurityPolicy policy = SecurityPolicy.builder()
				.passwordRequireUppercase(false)
				.passwordRequireNumber(false)
				.build();
		assertThatCode(() -> service.requireAcceptable(policy, "lowercaseonly")).doesNotThrowAnyException();
	}

	@Test
	void noExpiryMeansNeverExpired() {
		User user = User.builder().passwordChangedAt(LocalDateTime.now().minusYears(5)).build();
		assertThat(service.isExpired(SecurityPolicy.defaults(), user)).isFalse();
	}

	@Test
	void expiresOnceOlderThanThePolicyAllows() {
		SecurityPolicy policy = SecurityPolicy.builder().passwordExpiryDays(90).build();
		assertThat(service.isExpired(policy, User.builder().passwordChangedAt(LocalDateTime.now().minusDays(91)).build()))
				.isTrue();
		assertThat(service.isExpired(policy, User.builder().passwordChangedAt(LocalDateTime.now().minusDays(10)).build()))
				.isFalse();
	}

	@Test
	void anAccountThatNeverChangedItsPasswordCountsFromCreation() {
		SecurityPolicy policy = SecurityPolicy.builder().passwordExpiryDays(30).build();
		User user = User.builder().createdAt(LocalDateTime.now().minusDays(40)).build();
		assertThat(service.isExpired(policy, user)).isTrue();
	}
}

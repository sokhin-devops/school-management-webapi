package com.school_management_webapi.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/** Against RFC 6238's own test vectors, cut to the six digits the apps show. */
class TotpTest {

	private static final byte[] RFC_KEY = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

	@Test
	void matchesTheRfcVectors() {
		assertThat(Totp.codeAt(RFC_KEY, 59 / 30)).isEqualTo("287082");
		assertThat(Totp.codeAt(RFC_KEY, 1111111109L / 30)).isEqualTo("081804");
		assertThat(Totp.codeAt(RFC_KEY, 1234567890L / 30)).isEqualTo("005924");
	}

	@Test
	void base32RoundTrips() {
		String secret = Totp.encodeBase32(RFC_KEY);
		assertThat(secret).isEqualTo("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");
		assertThat(Totp.decodeBase32(secret)).isEqualTo(RFC_KEY);
	}

	@Test
	void acceptsTheCurrentCodeAndOneStepEitherSide() {
		String secret = Totp.encodeBase32(RFC_KEY);
		long now = 1111111109L;
		long step = now / 30;
		assertThat(Totp.matchingStep(secret, Totp.codeAt(RFC_KEY, step), now)).hasValue(step);
		assertThat(Totp.matchingStep(secret, Totp.codeAt(RFC_KEY, step - 1), now)).hasValue(step - 1);
		assertThat(Totp.matchingStep(secret, Totp.codeAt(RFC_KEY, step + 2), now)).isEmpty();
	}

	@Test
	void refusesWhatIsNotSixDigits() {
		String secret = Totp.newSecret();
		assertThat(Totp.matchingStep(secret, "12345", 0)).isEmpty();
		assertThat(Totp.matchingStep(secret, "abcdef", 0)).isEmpty();
		assertThat(Totp.matchingStep(null, "123456", 0)).isEmpty();
	}

	@Test
	void theQrCodeCarriesIssuerAccountAndSecret() {
		assertThat(Totp.otpauthUri("SchoolSuite", "ada@example.com", "ABC"))
				.isEqualTo("otpauth://totp/SchoolSuite:ada%40example.com?secret=ABC&issuer=SchoolSuite"
						+ "&algorithm=SHA1&digits=6&period=30");
	}
}

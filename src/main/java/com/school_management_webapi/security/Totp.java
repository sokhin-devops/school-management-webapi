package com.school_management_webapi.security;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.OptionalLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Time-based one-time passwords (RFC 6238) - the six digits an authenticator
 * app shows. HMAC-SHA1, 30-second steps, six digits: the defaults every app
 * assumes when it scans a code.
 */
public final class Totp {

	public static final int DIGITS = 6;
	public static final int PERIOD_SECONDS = 30;
	/** A step either side, so a phone clock a little out still signs in. */
	private static final int WINDOW = 1;
	private static final int SECRET_BYTES = 20;
	private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
	private static final SecureRandom RANDOM = new SecureRandom();

	private Totp() {
	}

	/** A new shared secret, base32 - what the app is given by the QR code or by hand. */
	public static String newSecret() {
		byte[] bytes = new byte[SECRET_BYTES];
		RANDOM.nextBytes(bytes);
		return encodeBase32(bytes);
	}

	/** What the QR code holds: the app reads the account, issuer and secret from it. */
	public static String otpauthUri(String issuer, String account, String secret) {
		String label = encode(issuer) + ":" + encode(account);
		return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + encode(issuer) + "&algorithm=SHA1&digits="
				+ DIGITS + "&period=" + PERIOD_SECONDS;
	}

	/**
	 * The step the code is valid for, when it is valid now. Returning the step
	 * rather than a yes lets the caller refuse a code that was already used.
	 */
	public static OptionalLong matchingStep(String secret, String code, long epochSeconds) {
		if (secret == null || code == null) {
			return OptionalLong.empty();
		}
		String digits = code.replaceAll("\\s", "");
		if (!digits.matches("\\d{" + DIGITS + "}")) {
			return OptionalLong.empty();
		}
		byte[] key = decodeBase32(secret);
		long current = epochSeconds / PERIOD_SECONDS;
		for (long step = current - WINDOW; step <= current + WINDOW; step++) {
			// Compared in constant time, like any other secret.
			if (MessageDigest.isEqual(codeAt(key, step).getBytes(StandardCharsets.US_ASCII),
					digits.getBytes(StandardCharsets.US_ASCII))) {
				return OptionalLong.of(step);
			}
		}
		return OptionalLong.empty();
	}

	/** RFC 4226's HOTP for one counter value. */
	static String codeAt(byte[] key, long step) {
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(new SecretKeySpec(key, "HmacSHA1"));
			byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(step).array());
			int offset = hash[hash.length - 1] & 0x0f;
			int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
					| ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
			int otp = binary % (int) Math.pow(10, DIGITS);
			return String.format("%0" + DIGITS + "d", otp);
		} catch (GeneralSecurityException ex) {
			throw new IllegalStateException("HmacSHA1 is not available", ex);
		}
	}

	static String encodeBase32(byte[] bytes) {
		StringBuilder out = new StringBuilder();
		int buffer = 0;
		int bits = 0;
		for (byte value : bytes) {
			buffer = (buffer << 8) | (value & 0xff);
			bits += 8;
			while (bits >= 5) {
				out.append(BASE32.charAt((buffer >> (bits - 5)) & 0x1f));
				bits -= 5;
			}
		}
		if (bits > 0) {
			out.append(BASE32.charAt((buffer << (5 - bits)) & 0x1f));
		}
		return out.toString();
	}

	static byte[] decodeBase32(String text) {
		String clean = text.replace("=", "").replace(" ", "").toUpperCase();
		ByteBuffer out = ByteBuffer.allocate(clean.length() * 5 / 8);
		int buffer = 0;
		int bits = 0;
		for (char c : clean.toCharArray()) {
			int value = BASE32.indexOf(c);
			if (value < 0) {
				throw new IllegalArgumentException("Not base32: " + c);
			}
			buffer = (buffer << 5) | value;
			bits += 5;
			if (bits >= 8) {
				out.put((byte) ((buffer >> (bits - 8)) & 0xff));
				bits -= 8;
			}
		}
		return out.array();
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}
}

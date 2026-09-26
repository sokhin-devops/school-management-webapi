package com.school_management_webapi.service;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Who is on the other end of the current request: their address and browser.
 *
 * Read from the request bound to this thread, so services can record it without
 * every controller passing it down. Outside a request - a scheduled job - both
 * are null.
 */
public final class RequestInfo {

	private RequestInfo() {
	}

	public static String ipAddress() {
		HttpServletRequest request = current();
		if (request == null) {
			return null;
		}
		// Behind a proxy the first hop is the client; the rest are proxies.
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return truncate(forwarded.split(",")[0].trim(), 64);
		}
		return truncate(request.getRemoteAddr(), 64);
	}

	public static String userAgent() {
		HttpServletRequest request = current();
		return request == null ? null : truncate(request.getHeader("User-Agent"), 400);
	}

	/** "Chrome on Windows" - enough to recognise a device, without a parsing library. */
	public static String describeDevice(String userAgent) {
		if (userAgent == null || userAgent.isBlank()) {
			return "Unknown device";
		}
		String ua = userAgent.toLowerCase();

		String browser;
		if (ua.contains("edg/")) {
			browser = "Edge";
		} else if (ua.contains("opr/") || ua.contains("opera")) {
			browser = "Opera";
		} else if (ua.contains("firefox/")) {
			browser = "Firefox";
		} else if (ua.contains("chrome/") || ua.contains("crios/")) {
			browser = "Chrome";
		} else if (ua.contains("safari/")) {
			browser = "Safari";
		} else if (ua.contains("postman") || ua.contains("curl") || ua.contains("python") || ua.contains("java/")) {
			browser = "API client";
		} else {
			browser = "Browser";
		}

		String system;
		if (ua.contains("iphone") || ua.contains("ipad")) {
			system = "iOS";
		} else if (ua.contains("android")) {
			system = "Android";
		} else if (ua.contains("windows")) {
			system = "Windows";
		} else if (ua.contains("mac os") || ua.contains("macintosh")) {
			system = "macOS";
		} else if (ua.contains("linux")) {
			system = "Linux";
		} else {
			return browser;
		}
		return browser + " on " + system;
	}

	private static HttpServletRequest current() {
		if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
			return attributes.getRequest();
		}
		return null;
	}

	private static String truncate(String value, int max) {
		if (value == null) {
			return null;
		}
		return value.length() <= max ? value : value.substring(0, max);
	}
}

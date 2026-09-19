package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error envelope. {@code success} is always {@code false} so clients can
 * branch on the same flag they use for {@link ApiResponse}.
 */
public record ErrorResponse(
		boolean success,
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		String path,
		Map<String, String> fieldErrors,
		String errorCode) {

	public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path,
			Map<String, String> fieldErrors, String errorCode) {
		this(false, timestamp, status, error, message, path, fieldErrors, errorCode);
	}
}

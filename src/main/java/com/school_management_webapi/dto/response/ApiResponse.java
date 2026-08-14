package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;

public record ApiResponse<T>(
		boolean success,
		String message,
		T data,
		LocalDateTime timestamp
) {

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data, LocalDateTime.now());
	}

	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>(false, message, null, LocalDateTime.now());
	}

	public static <T> ApiResponse<T> error(String message, T data) {
		return new ApiResponse<>(false, message, data, LocalDateTime.now());
	}
}

package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.UserStatus;

public record UserResponse(
		UUID id,
		String name,
		String email,
		UserStatus status,
		LocalDateTime emailVerifiedAt,
		LocalDateTime lastLoginAt,
		LocalDateTime createdAt,
		UUID tenantId) {
}

package com.school_management_webapi.mapper;

import java.util.UUID;

import com.school_management_webapi.dto.response.UserResponse;
import com.school_management_webapi.entity.User;

public final class UserMapper {

	private UserMapper() {
	}

	public static UserResponse toResponse(User user, UUID tenantId) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getStatus(),
				user.getEmailVerifiedAt(),
				user.getLastLoginAt(),
				user.getCreatedAt(),
				tenantId,
				user.isTwoFactorEnabled());
	}
}

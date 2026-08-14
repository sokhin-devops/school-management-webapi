package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.response.UserResponse;
import com.school_management_webapi.entity.User;

public final class UserMapper {

	private UserMapper() {
	}

	public static UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getStatus(),
				user.getEmailVerifiedAt(),
				user.getLastLoginAt(),
				user.getCreatedAt());
	}
}

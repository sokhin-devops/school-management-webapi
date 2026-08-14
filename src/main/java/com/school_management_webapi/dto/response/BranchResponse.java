package com.school_management_webapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.BranchStatus;

public record BranchResponse(
		UUID id,
		UUID schoolId,
		String name,
		String address,
		String phone,
		boolean mainBranch,
		BranchStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.response.BranchResponse;
import com.school_management_webapi.entity.Branch;

public final class BranchMapper {

	private BranchMapper() {
	}

	public static BranchResponse toResponse(Branch branch) {
		return new BranchResponse(
				branch.getId(),
				branch.getSchool().getId(),
				branch.getName(),
				branch.getAddress(),
				branch.getPhone(),
				branch.isMainBranch(),
				branch.getStatus(),
				branch.getCreatedAt(),
				branch.getUpdatedAt());
	}
}

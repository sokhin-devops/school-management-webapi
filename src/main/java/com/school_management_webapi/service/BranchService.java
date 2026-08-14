package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.dto.request.BranchRequest;
import com.school_management_webapi.dto.response.BranchResponse;

public interface BranchService {

	BranchResponse create(UUID userId, BranchRequest request);

	List<BranchResponse> list(UUID userId, UUID schoolId);

	BranchResponse getById(UUID userId, UUID branchId);

	BranchResponse update(UUID userId, UUID branchId, BranchRequest request);

	void delete(UUID userId, UUID branchId);
}

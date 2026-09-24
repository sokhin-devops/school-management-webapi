package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.dto.request.RoleRequest;
import com.school_management_webapi.dto.response.RoleResponse;

public interface RoleService {

	List<RoleResponse> list(UUID userId);

	RoleResponse getById(UUID userId, UUID id);

	RoleResponse create(UUID userId, RoleRequest request);

	RoleResponse update(UUID userId, UUID id, RoleRequest request);

	void delete(UUID userId, UUID id);
}

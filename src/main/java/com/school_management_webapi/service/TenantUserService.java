package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.dto.request.TenantUserInviteRequest;
import com.school_management_webapi.dto.request.TenantUserUpdateRequest;
import com.school_management_webapi.dto.response.TenantUserResponse;

public interface TenantUserService {

	List<TenantUserResponse> list(UUID userId);

	TenantUserResponse invite(UUID userId, TenantUserInviteRequest request);

	TenantUserResponse update(UUID userId, UUID targetUserId, TenantUserUpdateRequest request);

	void remove(UUID userId, UUID targetUserId);
}

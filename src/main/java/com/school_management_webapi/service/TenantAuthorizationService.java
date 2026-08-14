package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.TenantUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Basic tenant-membership guard. Only resolves and confirms tenant
 * membership - the full owner/admin permission tree lands in API 04 (Users
 * &amp; Roles); this is the hook later authorization checks plug into.
 */
@Service
@RequiredArgsConstructor
public class TenantAuthorizationService {

	private final TenantUserRepository tenantUserRepository;

	public UUID requireTenantId(UUID userId) {
		return tenantUserRepository.findByUserId(userId)
				.map(tenantUser -> tenantUser.getTenant().getId())
				.orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
						"User does not belong to a tenant"));
	}
}

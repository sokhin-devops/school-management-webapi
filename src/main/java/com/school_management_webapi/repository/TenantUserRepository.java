package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.TenantUser;

public interface TenantUserRepository extends JpaRepository<TenantUser, UUID> {

	Optional<TenantUser> findFirstByUserIdOrderByCreatedAtAsc(UUID userId);

	boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

	List<TenantUser> findByTenantIdOrderByCreatedAtAsc(UUID tenantId);

	Optional<TenantUser> findByTenantIdAndUserId(UUID tenantId, UUID userId);

	/** Read before a role is deleted, so a role still in use cannot be removed. */
	long countByRoleId(UUID roleId);
}

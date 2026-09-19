package com.school_management_webapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.TenantUser;

public interface TenantUserRepository extends JpaRepository<TenantUser, UUID> {

	Optional<TenantUser> findFirstByUserIdOrderByCreatedAtAsc(UUID userId);

	boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);
}

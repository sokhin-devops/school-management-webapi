package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.DefaultRoleType;
import com.school_management_webapi.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

	List<Role> findByTenantIdOrderByNameAsc(UUID tenantId);

	Optional<Role> findByIdAndTenantId(UUID id, UUID tenantId);

	Optional<Role> findByTenantIdAndNameIgnoreCase(UUID tenantId, String name);

	Optional<Role> findByTenantIdAndDefaultType(UUID tenantId, DefaultRoleType defaultType);

	boolean existsByTenantId(UUID tenantId);
}

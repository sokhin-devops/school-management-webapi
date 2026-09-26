package com.school_management_webapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.TenantSettings;

public interface TenantSettingsRepository extends JpaRepository<TenantSettings, UUID> {

	Optional<TenantSettings> findByTenantId(UUID tenantId);
}

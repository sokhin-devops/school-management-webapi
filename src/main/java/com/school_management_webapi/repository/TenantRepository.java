package com.school_management_webapi.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}

package com.school_management_webapi.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

	Page<AuditEvent> findByTenantIdOrderByOccurredAtDesc(UUID tenantId, Pageable pageable);
}

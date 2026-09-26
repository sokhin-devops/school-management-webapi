package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.SubscriptionInvoice;

public interface SubscriptionInvoiceRepository extends JpaRepository<SubscriptionInvoice, UUID> {

	List<SubscriptionInvoice> findByTenantIdOrderByIssuedAtDesc(UUID tenantId);

	Optional<SubscriptionInvoice> findByIdAndTenantId(UUID id, UUID tenantId);

	long countByTenantId(UUID tenantId);
}

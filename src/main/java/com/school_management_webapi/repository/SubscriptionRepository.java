package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.Subscription;
import com.school_management_webapi.entity.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	Optional<Subscription> findFirstByTenantIdOrderByStartAtDesc(UUID tenantId);

	Optional<Subscription> findFirstByTenantIdAndStatusInOrderByStartAtDesc(UUID tenantId,
			List<SubscriptionStatus> statuses);

	List<Subscription> findAllByTenantIdOrderByStartAtDesc(UUID tenantId);

	boolean existsByTenantIdAndStatusIn(UUID tenantId, List<SubscriptionStatus> statuses);
}

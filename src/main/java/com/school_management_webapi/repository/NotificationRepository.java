package com.school_management_webapi.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.school_management_webapi.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	List<Notification> findTop30ByRecipientUserIdAndTenantIdOrderByCreatedAtDesc(UUID recipientUserId, UUID tenantId);

	long countByRecipientUserIdAndTenantIdAndReadAtIsNull(UUID recipientUserId, UUID tenantId);

	Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

	@Modifying
	@Query("UPDATE Notification n SET n.readAt = :now WHERE n.recipientUserId = :userId AND n.tenantId = :tenantId AND n.readAt IS NULL")
	int markAllRead(UUID userId, UUID tenantId, LocalDateTime now);
}

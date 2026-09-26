package com.school_management_webapi.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.NotificationPreference;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

	List<NotificationPreference> findByUserId(UUID userId);

	List<NotificationPreference> findByUserIdInAndEventKey(Collection<UUID> userIds, String eventKey);
}

package com.school_management_webapi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.NotificationPreferencesRequest;
import com.school_management_webapi.dto.response.NotificationResponse;
import com.school_management_webapi.entity.Notification;
import com.school_management_webapi.entity.NotificationPreference;
import com.school_management_webapi.entity.PermissionAction;
import com.school_management_webapi.entity.TenantUser;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.repository.NotificationPreferenceRepository;
import com.school_management_webapi.repository.NotificationRepository;
import com.school_management_webapi.repository.TenantUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * In-app notifications and each person's choices about them - 65-notifications.md.
 *
 * Nothing sends email yet, so that channel's choice is stored for when it does
 * and reported as unavailable; in-app is the one that delivers today.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationPreferenceRepository preferenceRepository;
	private final TenantUserRepository tenantUserRepository;
	private final PermissionService permissionService;
	private final SideEffects sideEffects;

	/**
	 * Tells everyone in the actor's school who can see the module and has not
	 * turned the event off - everyone but the actor, who knows already.
	 *
	 * Only once the actor's change has committed, and never as part of it: a
	 * notification is a courtesy, and failing to send one must not undo the
	 * payment it was about.
	 */
	public void notifySchool(UUID actorUserId, NotificationEvent event, String message, String link) {
		sideEffects.afterCommit("notification " + event, () -> tenantUserRepository
				.findFirstByUserIdOrderByCreatedAtAsc(actorUserId).ifPresent(actor -> {
					UUID tenantId = actor.getTenant().getId();
					List<UUID> recipients = tenantUserRepository.findByTenantIdOrderByCreatedAtAsc(tenantId).stream()
							.map(member -> member.getUser().getId())
							.filter(userId -> !userId.equals(actorUserId))
							.filter(userId -> event.module() == null
									|| permissionService.isAllowed(userId, event.module(), PermissionAction.VIEW))
							.toList();
					deliver(tenantId, recipients, event, message, link);
				}));
	}

	/** Tells every member of a tenant, the actor included - for a whole-school notice. */
	public void notifyTenant(UUID tenantId, NotificationEvent event, String message, String link) {
		sideEffects.afterCommit("notification " + event, () -> deliver(tenantId,
				tenantUserRepository.findByTenantIdOrderByCreatedAtAsc(tenantId).stream()
						.map(member -> member.getUser().getId())
						.toList(),
				event, message, link));
	}

	/** Tells one person about something that concerns only them. */
	public void notifyUser(UUID userId, NotificationEvent event, String message, String link) {
		sideEffects.afterCommit("notification " + event, () -> tenantUserRepository
				.findFirstByUserIdOrderByCreatedAtAsc(userId)
				.ifPresent(membership -> deliver(membership.getTenant().getId(), List.of(userId), event, message,
						link)));
	}

	// --- the bell ---------------------------------------------------------------

	@Transactional(readOnly = true)
	public NotificationResponse.Inbox inbox(UUID userId) {
		UUID tenantId = tenantOf(userId);
		if (tenantId == null) {
			return new NotificationResponse.Inbox(0, List.of());
		}
		List<NotificationResponse.Item> items = notificationRepository
				.findTop30ByRecipientUserIdAndTenantIdOrderByCreatedAtDesc(userId, tenantId).stream()
				.map(NotificationService::toItem)
				.toList();
		long unread = notificationRepository.countByRecipientUserIdAndTenantIdAndReadAtIsNull(userId, tenantId);
		return new NotificationResponse.Inbox(unread, items);
	}

	@Transactional
	public void markRead(UUID userId, UUID notificationId) {
		Notification notification = notificationRepository.findByIdAndRecipientUserId(notificationId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
		if (notification.getReadAt() == null) {
			notification.setReadAt(LocalDateTime.now());
			notificationRepository.save(notification);
		}
	}

	@Transactional
	public void markAllRead(UUID userId) {
		UUID tenantId = tenantOf(userId);
		if (tenantId != null) {
			notificationRepository.markAllRead(userId, tenantId, LocalDateTime.now());
		}
	}

	// --- preferences ------------------------------------------------------------

	@Transactional(readOnly = true)
	public NotificationResponse.Preferences preferences(UUID userId) {
		Map<String, NotificationPreference> chosen = preferenceRepository.findByUserId(userId).stream()
				.collect(Collectors.toMap(NotificationPreference::getEventKey, Function.identity(), (a, b) -> a));

		List<NotificationResponse.PreferenceGroup> groups = new ArrayList<>();
		for (NotificationEvent.Group group : NotificationEvent.GROUPS) {
			List<NotificationResponse.Preference> preferences = new ArrayList<>();
			for (NotificationEvent event : NotificationEvent.values()) {
				if (!event.group().equals(group.key())) {
					continue;
				}
				NotificationPreference choice = chosen.get(event.key());
				preferences.add(new NotificationResponse.Preference(event.key(), event.title(), event.note(),
						choice == null || choice.isInApp(), choice != null && choice.isEmail()));
			}
			groups.add(new NotificationResponse.PreferenceGroup(group.key(), group.title(), group.description(),
					preferences));
		}
		return new NotificationResponse.Preferences(groups, false);
	}

	@Transactional
	public NotificationResponse.Preferences updatePreferences(UUID userId, NotificationPreferencesRequest request) {
		Map<String, NotificationPreference> existing = preferenceRepository.findByUserId(userId).stream()
				.collect(Collectors.toMap(NotificationPreference::getEventKey, Function.identity(), (a, b) -> a));

		for (NotificationPreferencesRequest.Choice choice : request.preferences()) {
			// An unknown key is ignored rather than stored: it would be a switch
			// for an event that nothing raises.
			if (NotificationEvent.fromKey(choice.key()).isEmpty()) {
				continue;
			}
			NotificationPreference row = existing.get(choice.key());
			if (row == null) {
				row = NotificationPreference.builder().userId(userId).eventKey(choice.key()).build();
			}
			row.setInApp(choice.inApp());
			row.setEmail(choice.email());
			preferenceRepository.save(row);
		}
		return preferences(userId);
	}

	// --- internals --------------------------------------------------------------

	private void deliver(UUID tenantId, List<UUID> recipients, NotificationEvent event, String message, String link) {
		if (recipients.isEmpty()) {
			return;
		}
		Map<UUID, NotificationPreference> choices = preferenceRepository
				.findByUserIdInAndEventKey(recipients, event.key()).stream()
				.collect(Collectors.toMap(NotificationPreference::getUserId, Function.identity(), (a, b) -> a));

		List<Notification> notifications = new ArrayList<>();
		for (UUID recipient : recipients) {
			NotificationPreference choice = choices.get(recipient);
			if (choice != null && !choice.isInApp()) {
				continue;
			}
			notifications.add(Notification.builder()
					.tenantId(tenantId)
					.recipientUserId(recipient)
					.eventKey(event.key())
					.title(event.title())
					.message(message)
					.link(link)
					.build());
		}
		notificationRepository.saveAll(notifications);
	}

	private UUID tenantOf(UUID userId) {
		return tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
				.map(TenantUser::getTenant)
				.map(tenant -> tenant.getId())
				.orElse(null);
	}

	private static NotificationResponse.Item toItem(Notification notification) {
		return new NotificationResponse.Item(notification.getId(), notification.getEventKey(), notification.getTitle(),
				notification.getMessage(), notification.getLink(), notification.getCreatedAt(),
				notification.getReadAt() != null);
	}
}

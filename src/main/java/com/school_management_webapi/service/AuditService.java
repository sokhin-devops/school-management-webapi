package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.response.SettingsResponse;
import com.school_management_webapi.entity.AuditAction;
import com.school_management_webapi.entity.AuditEvent;
import com.school_management_webapi.entity.User;
import com.school_management_webapi.repository.AuditEventRepository;
import com.school_management_webapi.repository.TenantUserRepository;
import com.school_management_webapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 67-security.md's audit log: who did what, from where.
 *
 * Written through SideEffects.now, in a transaction of its own, so an entry
 * survives the request that produced it failing afterwards - a failed sign-in
 * is exactly the kind of event that ends in an exception, and it is the one
 * most worth keeping - and so a failure to write it never fails the request.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

	private static final int MAX_PAGE = 200;

	private final AuditEventRepository auditEventRepository;
	private final TenantUserRepository tenantUserRepository;
	private final UserRepository userRepository;
	private final SideEffects sideEffects;

	/** Records an action by a signed-in person, in whichever tenant they belong to. */
	public void record(UUID actorUserId, AuditAction action, String detail) {
		String ipAddress = RequestInfo.ipAddress();
		sideEffects.now("audit " + action, () -> tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(actorUserId)
				.ifPresent(membership -> {
					String name = userRepository.findById(actorUserId).map(User::getName).orElse("Unknown user");
					save(membership.getTenant().getId(), actorUserId, name, action, detail, ipAddress);
				}));
	}

	/** For an attempt with no signed-in actor: the account it targeted, if it exists. */
	public void recordFor(User target, AuditAction action, String detail) {
		UUID targetId = target.getId();
		String targetName = target.getName();
		String ipAddress = RequestInfo.ipAddress();
		sideEffects.now("audit " + action, () -> tenantUserRepository.findFirstByUserIdOrderByCreatedAtAsc(targetId)
				.ifPresent(membership -> save(membership.getTenant().getId(), targetId, targetName, action, detail,
						ipAddress)));
	}

	@Transactional(readOnly = true)
	public List<SettingsResponse.AuditEntry> recent(UUID tenantId, int size) {
		int limit = Math.max(1, Math.min(size, MAX_PAGE));
		return auditEventRepository.findByTenantIdOrderByOccurredAtDesc(tenantId, PageRequest.of(0, limit))
				.map(event -> new SettingsResponse.AuditEntry(event.getId(), event.getActorName(),
						event.getAction(), event.getDetail(), event.getIpAddress(), event.getOccurredAt()))
				.getContent();
	}

	private void save(UUID tenantId, UUID actorUserId, String actorName, AuditAction action, String detail,
			String ipAddress) {
		auditEventRepository.save(AuditEvent.builder()
				.tenantId(tenantId)
				.actorUserId(actorUserId)
				.actorName(actorName)
				.action(action)
				.detail(detail == null ? null : detail.length() > 500 ? detail.substring(0, 500) : detail)
				.ipAddress(ipAddress)
				.build());
	}
}

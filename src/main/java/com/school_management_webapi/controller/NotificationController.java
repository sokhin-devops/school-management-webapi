package com.school_management_webapi.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.NotificationPreferencesRequest;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.NotificationResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** The caller's own notifications and choices - 65-notifications.md. */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	@Operation(summary = "Recent notifications for the current user, with the unread count")
	public ResponseEntity<ApiResponse<NotificationResponse.Inbox>> inbox(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Notifications retrieved",
				notificationService.inbox(principal.getUser().getId())));
	}

	@PostMapping("/{id}/read")
	@Operation(summary = "Mark one notification read")
	public ResponseEntity<Void> markRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		notificationService.markRead(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/read-all")
	@Operation(summary = "Mark every notification read")
	public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
		notificationService.markAllRead(principal.getUser().getId());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/preferences")
	@Operation(summary = "Every event the current user can be told about, and how")
	public ResponseEntity<ApiResponse<NotificationResponse.Preferences>> preferences(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success("Preferences retrieved",
				notificationService.preferences(principal.getUser().getId())));
	}

	@PutMapping("/preferences")
	@Operation(summary = "Save the current user's choices")
	public ResponseEntity<ApiResponse<NotificationResponse.Preferences>> updatePreferences(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody NotificationPreferencesRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Preferences saved",
				notificationService.updatePreferences(principal.getUser().getId(), request)));
	}
}

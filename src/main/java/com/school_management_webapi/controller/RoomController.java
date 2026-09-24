package com.school_management_webapi.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.RoomCreateRequest;
import com.school_management_webapi.dto.request.RoomPatchRequest;
import com.school_management_webapi.dto.request.RoomUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.RoomResponse;
import com.school_management_webapi.entity.RecordStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms")
public class RoomController {

	private final RoomService roomService;

	@GetMapping
	@Operation(summary = "List rooms of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<RoomResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) RecordStatus status,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(roomService.list(principal.getUser().getId(), branchId, status, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a room by id")
	public ResponseEntity<RoomResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(roomService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create a room")
	public ResponseEntity<RoomResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody RoomCreateRequest request) {
		RoomResponse created = roomService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/rooms/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a room")
	public ResponseEntity<RoomResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody RoomUpdateRequest request) {
		return ResponseEntity.ok(roomService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a room")
	public ResponseEntity<RoomResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody RoomPatchRequest request) {
		return ResponseEntity.ok(roomService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a room")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		roomService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

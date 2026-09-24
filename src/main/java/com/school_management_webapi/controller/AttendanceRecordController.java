package com.school_management_webapi.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.school_management_webapi.dto.request.AttendanceRecordCreateRequest;
import com.school_management_webapi.dto.request.AttendanceRecordPatchRequest;
import com.school_management_webapi.dto.request.AttendanceRecordUpdateRequest;
import com.school_management_webapi.dto.response.AttendanceRecordResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.AttendanceStatus;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.AttendanceRecordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance")
public class AttendanceRecordController {

	private final AttendanceRecordService attendanceRecordService;

	@GetMapping
	@Operation(summary = "List attendance records of the tenant with optional search and filters")
	public ResponseEntity<PagedResponse<AttendanceRecordResponse>> list(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) UUID branchId,
			@RequestParam(required = false) AttendanceStatus status,
			@RequestParam(required = false) UUID classGroupId,
			@RequestParam(required = false) UUID studentId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDateTo,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(attendanceRecordService.list(principal.getUser().getId(), branchId, status, classGroupId, studentId, attendanceDateFrom, attendanceDateTo, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get an attendance record by id")
	public ResponseEntity<AttendanceRecordResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id) {
		return ResponseEntity.ok(attendanceRecordService.getById(principal.getUser().getId(), id));
	}

	@PostMapping
	@Operation(summary = "Create an attendance record")
	public ResponseEntity<AttendanceRecordResponse> create(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody AttendanceRecordCreateRequest request) {
		AttendanceRecordResponse created = attendanceRecordService.create(principal.getUser().getId(), request);
		return ResponseEntity.created(URI.create("/api/v1/attendance/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace an attendance record")
	public ResponseEntity<AttendanceRecordResponse> update(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody AttendanceRecordUpdateRequest request) {
		return ResponseEntity.ok(attendanceRecordService.update(principal.getUser().getId(), id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update an attendance record")
	public ResponseEntity<AttendanceRecordResponse> patch(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id, @Valid @RequestBody AttendanceRecordPatchRequest request) {
		return ResponseEntity.ok(attendanceRecordService.patch(principal.getUser().getId(), id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) an attendance record")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
		attendanceRecordService.delete(principal.getUser().getId(), id);
		return ResponseEntity.noContent().build();
	}
}

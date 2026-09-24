package com.school_management_webapi.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.AttendanceRegisterRequest;
import com.school_management_webapi.dto.response.AttendanceRegisterResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.AttendanceRegisterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendance/register")
@RequiredArgsConstructor
@Tag(name = "Attendance")
public class AttendanceRegisterController {

	private final AttendanceRegisterService attendanceRegisterService;

	@GetMapping
	@Operation(summary = "Get the register of one class for one sitting, with its tallies")
	public ResponseEntity<AttendanceRegisterResponse> getRegister(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam UUID branchId,
			@RequestParam UUID classGroupId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
			@RequestParam(required = false) String session) {
		return ResponseEntity.ok(attendanceRegisterService.getRegister(principal.getUser().getId(), branchId,
				classGroupId, attendanceDate, session));
	}

	@PutMapping
	@Operation(summary = "Mark a whole class for one sitting, replacing whatever was recorded before")
	public ResponseEntity<AttendanceRegisterResponse> saveRegister(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody AttendanceRegisterRequest request) {
		return ResponseEntity.ok(attendanceRegisterService.saveRegister(principal.getUser().getId(), request));
	}
}

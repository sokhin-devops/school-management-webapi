package com.school_management_webapi.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.SettingsRequest;
import com.school_management_webapi.dto.response.ApiResponse;
import com.school_management_webapi.dto.response.SettingsResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.SystemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** 69-system-settings.md. Behind Settings in the role grid; deleting is owner only on top. */
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Tag(name = "System")
public class SystemController {

	private final SystemService systemService;

	@GetMapping("/info")
	@Operation(summary = "What this installation is running")
	public ResponseEntity<ApiResponse<SettingsResponse.SystemInfo>> info() {
		return ResponseEntity.ok(ApiResponse.success("System information retrieved", systemService.info()));
	}

	@GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Everything the school has entered, as one JSON file")
	public ResponseEntity<Map<String, Object>> export(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"school-export-" + LocalDate.now() + ".json\"")
				.body(systemService.export(principal.getUser().getId()));
	}

	@PostMapping("/delete-data")
	@Operation(summary = "Delete every student, staff, academic and finance record (owner only)")
	public ResponseEntity<ApiResponse<Map<String, Integer>>> deleteAllData(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody SettingsRequest.DeleteAllData request) {
		return ResponseEntity.ok(ApiResponse.success("School data deleted",
				systemService.deleteAllData(principal.getUser().getId(), request.confirmation())));
	}
}

package com.school_management_webapi.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.AssessmentScoreSheetRequest;
import com.school_management_webapi.dto.response.AssessmentScoreSheetResponse;
import com.school_management_webapi.security.UserPrincipal;
import com.school_management_webapi.service.AssessmentScoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}/scores")
@RequiredArgsConstructor
@Tag(name = "Assessments")
public class AssessmentScoreController {

	private final AssessmentScoreService assessmentScoreService;

	@GetMapping
	@Operation(summary = "Get the mark sheet of an assessment, with its average")
	public ResponseEntity<AssessmentScoreSheetResponse> getSheet(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID assessmentId) {
		return ResponseEntity.ok(assessmentScoreService.getSheet(principal.getUser().getId(), assessmentId));
	}

	@PutMapping
	@Operation(summary = "Replace the whole mark sheet and recompute the average of the assessment")
	public ResponseEntity<AssessmentScoreSheetResponse> saveSheet(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID assessmentId, @Valid @RequestBody AssessmentScoreSheetRequest request) {
		return ResponseEntity.ok(assessmentScoreService.saveSheet(principal.getUser().getId(), assessmentId, request));
	}
}

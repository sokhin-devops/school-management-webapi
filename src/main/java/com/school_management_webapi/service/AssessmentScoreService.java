package com.school_management_webapi.service;

import java.util.UUID;

import com.school_management_webapi.dto.request.AssessmentScoreSheetRequest;
import com.school_management_webapi.dto.response.AssessmentScoreSheetResponse;

public interface AssessmentScoreService {

	AssessmentScoreSheetResponse getSheet(UUID userId, UUID assessmentId);

	AssessmentScoreSheetResponse saveSheet(UUID userId, UUID assessmentId, AssessmentScoreSheetRequest request);
}

package com.school_management_webapi.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * The whole mark sheet for one assessment.
 *
 * Saving is a replacement rather than a merge: a student left out of the list
 * is a student who is no longer marked, which is what removing a row from the
 * sheet in front of you should mean.
 */
public record AssessmentScoreSheetRequest(
		@NotNull(message = "scores is required") @Valid List<AssessmentScoreEntryRequest> scores) {
}

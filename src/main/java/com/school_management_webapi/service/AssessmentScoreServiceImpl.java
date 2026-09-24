package com.school_management_webapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.AssessmentScoreEntryRequest;
import com.school_management_webapi.dto.request.AssessmentScoreSheetRequest;
import com.school_management_webapi.dto.response.AssessmentScoreResponse;
import com.school_management_webapi.dto.response.AssessmentScoreSheetResponse;
import com.school_management_webapi.entity.Assessment;
import com.school_management_webapi.entity.AssessmentScore;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.repository.AssessmentRepository;
import com.school_management_webapi.repository.AssessmentScoreRepository;

import lombok.RequiredArgsConstructor;

/**
 * Marking, and the two numbers an assessment carries because of it.
 *
 * averageScore and graded are stored on the assessment rather than counted on
 * every read, because every list of assessments shows them and a mark sheet is
 * written far less often than it is looked at.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentScoreServiceImpl implements AssessmentScoreService {

	private final AssessmentRepository assessmentRepository;
	private final AssessmentScoreRepository assessmentScoreRepository;
	private final BranchScopeService branchScopeService;

	@Override
	@Transactional(readOnly = true)
	public AssessmentScoreSheetResponse getSheet(UUID userId, UUID assessmentId) {
		Assessment assessment = findAssessmentInTenantOrThrow(userId, assessmentId);
		return toResponse(assessment, assessmentScoreRepository.findByAssessmentIdOrderByCreatedAtAsc(assessmentId));
	}

	@Override
	public AssessmentScoreSheetResponse saveSheet(UUID userId, UUID assessmentId,
			AssessmentScoreSheetRequest request) {
		Assessment assessment = findAssessmentInTenantOrThrow(userId, assessmentId);
		rejectDuplicateStudents(request.scores());
		rejectScoresOverMaximum(request.scores(), assessment.getMaxScore());

		// Replaced rather than merged: a student left out of the sheet is a student
		// who is no longer marked.
		assessmentScoreRepository.deleteByAssessmentId(assessmentId);
		assessmentScoreRepository.flush();

		List<AssessmentScore> saved = assessmentScoreRepository.saveAll(request.scores().stream()
				.map(entry -> AssessmentScore.builder()
						.assessmentId(assessmentId)
						.studentId(entry.studentId())
						.score(entry.score())
						.remark(entry.remark())
						.build())
				.toList());

		assessment.setAverageScore(averageOf(saved));
		assessment.setGraded(saved.stream().anyMatch(score -> score.getScore() != null));
		assessmentRepository.saveAndFlush(assessment);

		return toResponse(assessment, saved);
	}

	/**
	 * An unmarked student is left out of the average rather than counted as zero,
	 * which would drag a half-marked sheet down and make it look worse than it is.
	 */
	private BigDecimal averageOf(List<AssessmentScore> scores) {
		List<BigDecimal> marked = scores.stream()
				.map(AssessmentScore::getScore)
				.filter(score -> score != null)
				.toList();

		if (marked.isEmpty()) {
			return null;
		}

		BigDecimal total = marked.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		return total.divide(BigDecimal.valueOf(marked.size()), 2, RoundingMode.HALF_UP);
	}

	private void rejectDuplicateStudents(List<AssessmentScoreEntryRequest> scores) {
		Set<UUID> seen = new HashSet<>();
		List<UUID> duplicates = new ArrayList<>();
		for (AssessmentScoreEntryRequest entry : scores) {
			if (!seen.add(entry.studentId())) {
				duplicates.add(entry.studentId());
			}
		}
		if (!duplicates.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "DUPLICATE_STUDENT_SCORE",
					"A student appears more than once in the sheet: " + duplicates.get(0));
		}
	}

	private void rejectScoresOverMaximum(List<AssessmentScoreEntryRequest> scores, Integer maxScore) {
		if (maxScore == null) {
			return;
		}
		BigDecimal ceiling = BigDecimal.valueOf(maxScore);
		boolean overMaximum = scores.stream()
				.anyMatch(entry -> entry.score() != null && entry.score().compareTo(ceiling) > 0);
		if (overMaximum) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "SCORE_ABOVE_MAXIMUM",
					"A score is higher than the maximum of " + maxScore + " set for this assessment");
		}
	}

	private Assessment findAssessmentInTenantOrThrow(UUID userId, UUID assessmentId) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Assessment assessment = assessmentRepository.findById(assessmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

		branchScopeService.requireBranchInTenant(assessment.getBranchId(), tenantId);
		return assessment;
	}

	private AssessmentScoreSheetResponse toResponse(Assessment assessment, List<AssessmentScore> scores) {
		return new AssessmentScoreSheetResponse(
				assessment.getId(),
				assessment.getMaxScore(),
				assessment.getAverageScore(),
				assessment.getGraded(),
				scores.stream()
						.map(score -> new AssessmentScoreResponse(score.getId(), score.getStudentId(),
								score.getScore(), score.getRemark()))
						.toList());
	}
}

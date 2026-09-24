package com.school_management_webapi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.AssessmentScore;

public interface AssessmentScoreRepository extends JpaRepository<AssessmentScore, UUID> {

	List<AssessmentScore> findByAssessmentIdOrderByCreatedAtAsc(UUID assessmentId);

	void deleteByAssessmentId(UUID assessmentId);

	List<AssessmentScore> findByStudentId(UUID studentId);
}

package com.school_management_webapi.entity;

/**
 * The kind of assessment. 31-exams-and-grades.md: schools differ, so this
 * stays coarse and the grading scale carries the detail.
 */
public enum AssessmentType {
	QUIZ,
	MIDTERM,
	FINAL,
	ASSIGNMENT
}

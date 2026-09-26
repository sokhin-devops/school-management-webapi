package com.school_management_webapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 63-academic-settings.md: which academic concepts a school uses, and what it
 * calls them.
 *
 * 20-academic.md - "Do not assume every school needs every concept." Classes
 * are not switchable: attendance, assessments and enrolment all hang off them.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicPreferences {

	@Column(name = "use_programs", nullable = false)
	@Builder.Default
	private boolean usePrograms = true;

	@Column(name = "use_levels", nullable = false)
	@Builder.Default
	private boolean useLevels = true;

	/** Sections are classes inside a class - the parent-class link. */
	@Column(name = "use_sections", nullable = false)
	@Builder.Default
	private boolean useSections = true;

	@Column(name = "use_subjects", nullable = false)
	@Builder.Default
	private boolean useSubjects = true;

	@Column(name = "use_terms", nullable = false)
	@Builder.Default
	private boolean useTerms = true;

	@Column(name = "use_rooms", nullable = false)
	@Builder.Default
	private boolean useRooms = true;

	@Column(name = "level_label", nullable = false, length = 40)
	@Builder.Default
	private String levelLabel = "Level";

	@Column(name = "class_label", nullable = false, length = 40)
	@Builder.Default
	private String classLabel = "Class";

	@Column(name = "subject_label", nullable = false, length = 40)
	@Builder.Default
	private String subjectLabel = "Subject";

	@Column(name = "student_label", nullable = false, length = 40)
	@Builder.Default
	private String studentLabel = "Student";

	@Column(name = "teacher_label", nullable = false, length = 40)
	@Builder.Default
	private String teacherLabel = "Teacher";

	/** When the active year ends, make the next upcoming one active. */
	@Column(name = "auto_rollover", nullable = false)
	@Builder.Default
	private boolean autoRollover = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "term_structure", nullable = false, length = 20)
	@Builder.Default
	private TermStructure termStructure = TermStructure.SEMESTERS;

	@Enumerated(EnumType.STRING)
	@Column(name = "grading_scale", nullable = false, length = 20)
	@Builder.Default
	private GradingScale gradingScale = GradingScale.PERCENTAGE;

	/** The share of the marks, as a percentage, that counts as a pass. */
	@Column(name = "pass_mark", nullable = false)
	@Builder.Default
	private int passMark = 50;

	public static AcademicPreferences defaults() {
		return AcademicPreferences.builder().build();
	}
}

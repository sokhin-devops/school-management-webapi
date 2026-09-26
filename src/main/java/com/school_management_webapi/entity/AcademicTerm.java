package com.school_management_webapi.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A semester, term or quarter inside an academic year - 25-academic-years.md:
 * "Institutions may additionally use Semesters or Terms."
 *
 * Owned by its year rather than a record of its own: a term means nothing
 * outside the year it divides, and is only ever edited as part of that year.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AcademicTerm {

	@Column(name = "name", nullable = false, length = 60)
	private String name;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;
}

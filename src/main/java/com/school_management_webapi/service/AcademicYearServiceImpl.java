package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.AcademicYearRequest;
import com.school_management_webapi.dto.response.AcademicYearResponse;
import com.school_management_webapi.entity.AcademicTerm;
import com.school_management_webapi.entity.AcademicYear;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.mapper.AcademicYearMapper;
import com.school_management_webapi.repository.AcademicYearRepository;
import com.school_management_webapi.repository.SchoolRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademicYearServiceImpl implements AcademicYearService {

	private final AcademicYearRepository academicYearRepository;
	private final SchoolRepository schoolRepository;
	private final TenantAuthorizationService tenantAuthorizationService;
	private final CurrentSubscriptionResolver currentSubscriptionResolver;

	@Override
	public AcademicYearResponse create(UUID userId, AcademicYearRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		currentSubscriptionResolver.resolveActive(tenantId);

		School school = resolveSchoolForTenant(request.schoolId(), tenantId);
		validateDateRange(request.startDate(), request.endDate());

		String name = request.name().trim();
		ensureNameIsFree(school.getId(), name, null);
		ensureNoOverlap(school.getId(), request.startDate(), request.endDate(), null);

		boolean makeCurrent = request.current() || !academicYearRepository.existsBySchoolId(school.getId());
		if (makeCurrent) {
			unsetExistingCurrent(school.getId(), null);
		}

		AcademicYear academicYear = AcademicYear.builder()
				.school(school)
				.name(name)
				.startDate(request.startDate())
				.endDate(request.endDate())
				.terms(toTerms(request))
				.current(makeCurrent)
				.build();

		return AcademicYearMapper.toResponse(academicYearRepository.saveAndFlush(academicYear));
	}

	@Override
	@Transactional(readOnly = true)
	public List<AcademicYearResponse> list(UUID userId, UUID schoolId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		List<AcademicYear> years = schoolId != null
				? academicYearRepository.findBySchoolIdOrderByStartDateDesc(
						resolveSchoolForTenant(schoolId, tenantId).getId())
				: academicYearRepository.findAllByTenantId(tenantId);
		return years.stream().map(AcademicYearMapper::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public AcademicYearResponse getById(UUID userId, UUID academicYearId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return AcademicYearMapper.toResponse(findAcademicYearOrThrow(academicYearId, tenantId));
	}

	@Override
	public AcademicYearResponse update(UUID userId, UUID academicYearId, AcademicYearRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		AcademicYear academicYear = findAcademicYearOrThrow(academicYearId, tenantId);
		UUID schoolId = academicYear.getSchool().getId();

		validateDateRange(request.startDate(), request.endDate());

		String name = request.name().trim();
		ensureNameIsFree(schoolId, name, academicYear.getId());
		ensureNoOverlap(schoolId, request.startDate(), request.endDate(), academicYear.getId());

		if (request.current()) {
			unsetExistingCurrent(schoolId, academicYear.getId());
		} else if (academicYear.isCurrent()) {
			// A school always has exactly one current year; switching happens by
			// marking a different year current, not by clearing this one.
			throw new ApiException(HttpStatus.CONFLICT, "CURRENT_ACADEMIC_YEAR_REQUIRED",
					"A school must have one current academic year. Mark another year as current instead.");
		}

		academicYear.setName(name);
		academicYear.setStartDate(request.startDate());
		academicYear.setEndDate(request.endDate());
		if (request.terms() != null) {
			academicYear.getTerms().clear();
			academicYear.getTerms().addAll(toTerms(request));
		} else {
			// Terms kept as they were still have to fit the year's new dates.
			requireTermsInside(academicYear.getTerms(), request.startDate(), request.endDate());
		}
		academicYear.setCurrent(request.current());

		return AcademicYearMapper.toResponse(academicYearRepository.saveAndFlush(academicYear));
	}

	@Override
	public void delete(UUID userId, UUID academicYearId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		AcademicYear academicYear = findAcademicYearOrThrow(academicYearId, tenantId);
		UUID schoolId = academicYear.getSchool().getId();

		AcademicYear promoted = academicYear.isCurrent()
				? academicYearRepository.findBySchoolIdOrderByStartDateDesc(schoolId).stream()
						.filter(remaining -> !remaining.getId().equals(academicYear.getId()))
						.findFirst()
						.orElse(null)
				: null;

		academicYearRepository.delete(academicYear);

		if (promoted != null) {
			promoted.setCurrent(true);
			academicYearRepository.save(promoted);
		}
	}

	private AcademicYear findAcademicYearOrThrow(UUID academicYearId, UUID tenantId) {
		return academicYearRepository.findByIdAndTenantId(academicYearId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACADEMIC_YEAR_NOT_FOUND",
						"Academic year not found."));
	}

	private School resolveSchoolForTenant(UUID schoolId, UUID tenantId) {
		return schoolRepository.findByIdAndTenantId(schoolId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SCHOOL_NOT_FOUND", "School not found."));
	}

	/**
	 * The request's terms, checked: each inside the year, each ending after it
	 * starts, and none overlapping the next - a date can only be in one term.
	 */
	private List<AcademicTerm> toTerms(AcademicYearRequest request) {
		if (request.terms() == null || request.terms().isEmpty()) {
			return new ArrayList<>();
		}
		List<AcademicTerm> terms = request.terms().stream()
				.map(term -> new AcademicTerm(term.name().trim(), term.startDate(), term.endDate()))
				.sorted(java.util.Comparator.comparing(AcademicTerm::getStartDate))
				.collect(java.util.stream.Collectors.toCollection(ArrayList::new));
		requireTermsInside(terms, request.startDate(), request.endDate());
		for (int i = 1; i < terms.size(); i++) {
			if (!terms.get(i).getStartDate().isAfter(terms.get(i - 1).getEndDate())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "TERMS_OVERLAP",
						terms.get(i - 1).getName() + " and " + terms.get(i).getName() + " overlap.");
			}
		}
		return terms;
	}

	private void requireTermsInside(List<AcademicTerm> terms, LocalDate yearStart, LocalDate yearEnd) {
		for (AcademicTerm term : terms) {
			if (term.getEndDate().isBefore(term.getStartDate())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TERM",
						term.getName() + " ends before it starts.");
			}
			if (term.getStartDate().isBefore(yearStart) || term.getEndDate().isAfter(yearEnd)) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "TERM_OUTSIDE_YEAR",
						term.getName() + " falls outside the academic year.");
			}
		}
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (!endDate.isAfter(startDate)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "endDate must be after startDate.");
		}
	}

	private void ensureNameIsFree(UUID schoolId, String name, UUID excludedId) {
		academicYearRepository.findBySchoolIdAndNameIgnoreCase(schoolId, name)
				.filter(existing -> !existing.getId().equals(excludedId))
				.ifPresent(existing -> {
					throw new ApiException(HttpStatus.CONFLICT, "ACADEMIC_YEAR_NAME_ALREADY_EXISTS",
							"An academic year named '" + name + "' already exists for this school.");
				});
	}

	private void ensureNoOverlap(UUID schoolId, LocalDate startDate, LocalDate endDate, UUID excludedId) {
		List<AcademicYear> overlapping = academicYearRepository.findOverlapping(schoolId, startDate, endDate,
				excludedId);
		if (!overlapping.isEmpty()) {
			AcademicYear clash = overlapping.get(0);
			throw new ApiException(HttpStatus.CONFLICT, "ACADEMIC_YEAR_OVERLAP",
					"The date range overlaps academic year '" + clash.getName() + "' ("
							+ clash.getStartDate() + " to " + clash.getEndDate() + ").");
		}
	}

	private void unsetExistingCurrent(UUID schoolId, UUID excludedId) {
		academicYearRepository.findBySchoolIdOrderByStartDateDesc(schoolId).stream()
				.filter(AcademicYear::isCurrent)
				.filter(existing -> !existing.getId().equals(excludedId))
				.forEach(existing -> {
					existing.setCurrent(false);
					academicYearRepository.save(existing);
				});
	}
}

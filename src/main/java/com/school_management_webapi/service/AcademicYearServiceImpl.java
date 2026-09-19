package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.AcademicYearRequest;
import com.school_management_webapi.dto.response.AcademicYearResponse;
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

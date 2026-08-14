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

	@Override
	public AcademicYearResponse create(UUID userId, AcademicYearRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		School school = resolveSchoolForTenant(request.schoolId(), tenantId);
		validateDateRange(request.startDate(), request.endDate());

		boolean makeCurrent = request.current() || !academicYearRepository.existsBySchoolId(school.getId());
		if (makeCurrent) {
			unsetExistingCurrent(school.getId());
		}

		AcademicYear academicYear = AcademicYear.builder()
				.school(school)
				.name(request.name())
				.startDate(request.startDate())
				.endDate(request.endDate())
				.current(makeCurrent)
				.build();

		return AcademicYearMapper.toResponse(academicYearRepository.save(academicYear));
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
		validateDateRange(request.startDate(), request.endDate());

		if (request.current()) {
			unsetExistingCurrent(academicYear.getSchool().getId());
		}

		academicYear.setName(request.name());
		academicYear.setStartDate(request.startDate());
		academicYear.setEndDate(request.endDate());
		academicYear.setCurrent(request.current());

		return AcademicYearMapper.toResponse(academicYearRepository.save(academicYear));
	}

	@Override
	public void delete(UUID userId, UUID academicYearId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		academicYearRepository.delete(findAcademicYearOrThrow(academicYearId, tenantId));
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

	private void unsetExistingCurrent(UUID schoolId) {
		academicYearRepository.findBySchoolIdOrderByStartDateDesc(schoolId).stream()
				.filter(AcademicYear::isCurrent)
				.forEach(existing -> {
					existing.setCurrent(false);
					academicYearRepository.save(existing);
				});
	}
}

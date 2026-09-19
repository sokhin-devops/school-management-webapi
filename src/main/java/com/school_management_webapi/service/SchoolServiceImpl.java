package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.SchoolRequest;
import com.school_management_webapi.dto.response.SchoolResponse;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.entity.SchoolStatus;
import com.school_management_webapi.entity.Tenant;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.mapper.SchoolMapper;
import com.school_management_webapi.repository.AcademicYearRepository;
import com.school_management_webapi.repository.BranchRepository;
import com.school_management_webapi.repository.SchoolRepository;
import com.school_management_webapi.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolServiceImpl implements SchoolService {

	private final SchoolRepository schoolRepository;
	private final BranchRepository branchRepository;
	private final AcademicYearRepository academicYearRepository;
	private final TenantRepository tenantRepository;
	private final TenantAuthorizationService tenantAuthorizationService;
	private final CurrentSubscriptionResolver currentSubscriptionResolver;

	@Override
	public SchoolResponse create(UUID userId, SchoolRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		// Onboarding runs after plan selection, so a school can only be created once
		// the tenant is on a live subscription.
		currentSubscriptionResolver.resolveActive(tenantId);

		String name = request.name().trim();
		ensureSchoolNameIsFree(tenantId, name, null);

		Tenant tenant = tenantRepository.getReferenceById(tenantId);

		School school = School.builder()
				.tenant(tenant)
				.name(name)
				.type(request.type())
				.email(request.email().trim())
				.phone(request.phone().trim())
				.address(request.address().trim())
				.status(SchoolStatus.ACTIVE)
				.build();

		return SchoolMapper.toResponse(schoolRepository.saveAndFlush(school));
	}

	@Override
	@Transactional(readOnly = true)
	public List<SchoolResponse> list(UUID userId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return schoolRepository.findByTenantIdOrderByCreatedAtAsc(tenantId).stream()
				.map(SchoolMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public SchoolResponse getById(UUID userId, UUID schoolId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		return SchoolMapper.toResponse(findSchoolOrThrow(schoolId, tenantId));
	}

	@Override
	public SchoolResponse update(UUID userId, UUID schoolId, SchoolRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		School school = findSchoolOrThrow(schoolId, tenantId);

		String name = request.name().trim();
		ensureSchoolNameIsFree(tenantId, name, school.getId());

		school.setName(name);
		school.setType(request.type());
		school.setEmail(request.email().trim());
		school.setPhone(request.phone().trim());
		school.setAddress(request.address().trim());

		return SchoolMapper.toResponse(schoolRepository.saveAndFlush(school));
	}

	@Override
	public void delete(UUID userId, UUID schoolId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		School school = findSchoolOrThrow(schoolId, tenantId);

		// Branches and academic years are meaningless without their school, and the
		// soft-delete filter on School would otherwise leave them permanently
		// unreachable but still counted against the plan's branch limit.
		branchRepository.deleteAll(branchRepository.findBySchoolIdOrderByCreatedAtAsc(school.getId()));
		academicYearRepository.deleteAll(academicYearRepository.findBySchoolIdOrderByStartDateDesc(school.getId()));

		schoolRepository.delete(school);
	}

	private School findSchoolOrThrow(UUID schoolId, UUID tenantId) {
		return schoolRepository.findByIdAndTenantId(schoolId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SCHOOL_NOT_FOUND", "School not found."));
	}

	private void ensureSchoolNameIsFree(UUID tenantId, String name, UUID excludedSchoolId) {
		schoolRepository.findByTenantIdAndNameIgnoreCase(tenantId, name)
				.filter(existing -> !existing.getId().equals(excludedSchoolId))
				.ifPresent(existing -> {
					throw new ApiException(HttpStatus.CONFLICT, "SCHOOL_NAME_ALREADY_EXISTS",
							"A school named '" + name + "' already exists for this tenant.");
				});
	}
}

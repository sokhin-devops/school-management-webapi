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
import com.school_management_webapi.repository.SchoolRepository;
import com.school_management_webapi.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolServiceImpl implements SchoolService {

	private final SchoolRepository schoolRepository;
	private final TenantRepository tenantRepository;
	private final TenantAuthorizationService tenantAuthorizationService;

	@Override
	public SchoolResponse create(UUID userId, SchoolRequest request) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		Tenant tenant = tenantRepository.getReferenceById(tenantId);

		School school = School.builder()
				.tenant(tenant)
				.name(request.name())
				.type(request.type())
				.email(request.email())
				.phone(request.phone())
				.address(request.address())
				.status(SchoolStatus.ACTIVE)
				.build();

		return SchoolMapper.toResponse(schoolRepository.save(school));
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

		school.setName(request.name());
		school.setType(request.type());
		school.setEmail(request.email());
		school.setPhone(request.phone());
		school.setAddress(request.address());

		return SchoolMapper.toResponse(schoolRepository.save(school));
	}

	@Override
	public void delete(UUID userId, UUID schoolId) {
		UUID tenantId = tenantAuthorizationService.requireTenantId(userId);
		schoolRepository.delete(findSchoolOrThrow(schoolId, tenantId));
	}

	private School findSchoolOrThrow(UUID schoolId, UUID tenantId) {
		return schoolRepository.findByIdAndTenantId(schoolId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SCHOOL_NOT_FOUND", "School not found."));
	}
}

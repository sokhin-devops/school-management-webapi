package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.response.PlanResponse;
import com.school_management_webapi.entity.Plan;
import com.school_management_webapi.entity.PlanStatus;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.mapper.PlanMapper;
import com.school_management_webapi.repository.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanServiceImpl implements PlanService {

	private final PlanRepository planRepository;

	@Override
	public List<PlanResponse> listActivePlans() {
		return planRepository.findAllWithFeaturesByStatus(PlanStatus.ACTIVE).stream()
				.map(PlanMapper::toResponse)
				.toList();
	}

	@Override
	public PlanResponse getPlanById(UUID planId) {
		Plan plan = planRepository.findByIdWithFeatures(planId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PLAN_NOT_FOUND", "Plan not found."));
		return PlanMapper.toResponse(plan);
	}
}

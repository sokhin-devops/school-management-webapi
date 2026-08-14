package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import com.school_management_webapi.dto.response.PlanResponse;

public interface PlanService {

	List<PlanResponse> listActivePlans();

	PlanResponse getPlanById(UUID planId);
}

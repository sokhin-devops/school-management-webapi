package com.school_management_webapi.service;

import java.util.UUID;

import com.school_management_webapi.dto.response.DashboardSummaryResponse;

public interface DashboardService {

	/**
	 * @param branchId one branch, or null for every branch the tenant owns.
	 */
	DashboardSummaryResponse summary(UUID userId, UUID branchId);
}

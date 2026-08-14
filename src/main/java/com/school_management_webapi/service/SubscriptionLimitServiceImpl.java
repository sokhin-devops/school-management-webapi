package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.entity.Plan;
import com.school_management_webapi.exception.ApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionLimitServiceImpl implements SubscriptionLimitService {

	private final CurrentSubscriptionResolver currentSubscriptionResolver;

	@Override
	public void checkLimit(UUID tenantId, LimitType limitType, long currentCount) {
		Plan plan = currentSubscriptionResolver.resolveActive(tenantId).getPlan();

		Integer limit = switch (limitType) {
			case STUDENTS -> plan.getMaxStudents();
			case TEACHERS -> plan.getMaxTeachers();
			case BRANCHES -> plan.getMaxBranches();
		};

		if (limit == null || currentCount < limit) {
			return;
		}

		String errorCode = switch (limitType) {
			case STUDENTS -> "STUDENT_LIMIT_REACHED";
			case TEACHERS -> "TEACHER_LIMIT_REACHED";
			case BRANCHES -> "BRANCH_LIMIT_REACHED";
		};
		String label = switch (limitType) {
			case STUDENTS -> "Student";
			case TEACHERS -> "Teacher";
			case BRANCHES -> "Branch";
		};

		throw new ApiException(HttpStatus.FORBIDDEN, errorCode,
				label + " limit reached for your current subscription.");
	}
}

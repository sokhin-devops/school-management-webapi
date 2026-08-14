package com.school_management_webapi.service;

import java.util.UUID;

public interface SubscriptionLimitService {

	void checkLimit(UUID tenantId, LimitType limitType, long currentCount);
}

package com.school_management_webapi.service;

import java.util.UUID;

public interface FeatureAccessService {

	boolean hasFeature(UUID tenantId, String featureCode);

	void requireFeature(UUID tenantId, String featureCode);
}

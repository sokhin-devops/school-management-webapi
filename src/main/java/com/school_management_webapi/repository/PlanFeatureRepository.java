package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school_management_webapi.entity.PlanFeature;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, UUID> {

	List<PlanFeature> findByPlanId(UUID planId);

	Optional<PlanFeature> findByPlanIdAndFeatureCode(UUID planId, String featureCode);
}

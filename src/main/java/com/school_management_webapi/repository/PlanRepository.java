package com.school_management_webapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.school_management_webapi.entity.Plan;
import com.school_management_webapi.entity.PlanStatus;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

	Optional<Plan> findByCode(String code);

	@Query("SELECT DISTINCT p FROM Plan p LEFT JOIN FETCH p.features WHERE p.status = :status ORDER BY p.sortOrder ASC")
	List<Plan> findAllWithFeaturesByStatus(@Param("status") PlanStatus status);

	@Query("SELECT p FROM Plan p LEFT JOIN FETCH p.features WHERE p.id = :id")
	Optional<Plan> findByIdWithFeatures(@Param("id") UUID id);
}

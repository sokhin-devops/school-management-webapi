package com.school_management_webapi.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.school_management_webapi.entity.Assessment;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID>, JpaSpecificationExecutor<Assessment> {

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);
}

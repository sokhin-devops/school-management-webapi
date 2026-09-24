package com.school_management_webapi.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.school_management_webapi.entity.Fee;

public interface FeeRepository extends JpaRepository<Fee, UUID>, JpaSpecificationExecutor<Fee> {

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);
}

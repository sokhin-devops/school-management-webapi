package com.school_management_webapi.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.school_management_webapi.entity.Level;

public interface LevelRepository extends JpaRepository<Level, UUID>, JpaSpecificationExecutor<Level> {

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);
}

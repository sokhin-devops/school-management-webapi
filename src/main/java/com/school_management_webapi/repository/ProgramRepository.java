package com.school_management_webapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.school_management_webapi.entity.Program;

public interface ProgramRepository extends JpaRepository<Program, UUID>, JpaSpecificationExecutor<Program> {

	boolean existsByCodeAndBranchId(String code, UUID branchId);

	Optional<Program> findByCodeAndBranchId(String code, UUID branchId);

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);
}

package com.school_management_webapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.school_management_webapi.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, UUID>, JpaSpecificationExecutor<Subject> {

	boolean existsByCodeAndBranchId(String code, UUID branchId);

	Optional<Subject> findByCodeAndBranchId(String code, UUID branchId);

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);
}

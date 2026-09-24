package com.school_management_webapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.school_management_webapi.entity.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, UUID>, JpaSpecificationExecutor<Teacher> {

	boolean existsByEmployeeNumberAndBranchId(String employeeNumber, UUID branchId);

	Optional<Teacher> findByEmployeeNumberAndBranchId(String employeeNumber, UUID branchId);

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);
}

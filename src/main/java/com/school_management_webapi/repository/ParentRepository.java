package com.school_management_webapi.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.school_management_webapi.entity.Parent;

public interface ParentRepository extends JpaRepository<Parent, UUID>, JpaSpecificationExecutor<Parent> {

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);

	List<Parent> findByBranchIdIn(Collection<UUID> branchIds);
}

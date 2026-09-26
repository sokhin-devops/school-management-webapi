package com.school_management_webapi.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.school_management_webapi.entity.Room;

public interface RoomRepository extends JpaRepository<Room, UUID>, JpaSpecificationExecutor<Room> {

	boolean existsByCodeAndBranchId(String code, UUID branchId);

	Optional<Room> findByCodeAndBranchId(String code, UUID branchId);

	long countByBranchIdIn(java.util.Collection<UUID> branchIds);

	List<Room> findByBranchIdIn(Collection<UUID> branchIds);
}

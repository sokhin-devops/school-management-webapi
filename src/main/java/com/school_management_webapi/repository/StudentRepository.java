package com.school_management_webapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.school_management_webapi.entity.Student;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {

	boolean existsByStudentCodeAndSchoolId(String studentCode, UUID schoolId);

	Optional<Student> findByStudentCodeAndSchoolId(String studentCode, UUID schoolId);
}

package com.school_management_webapi.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school_management_webapi.dto.request.StudentCreateRequest;
import com.school_management_webapi.dto.request.StudentPatchRequest;
import com.school_management_webapi.dto.request.StudentUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.StudentResponse;
import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.StudentStatus;
import com.school_management_webapi.service.StudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Students")
public class StudentController {

	private final StudentService studentService;

	@GetMapping
	@Operation(summary = "List students with optional search and filters")
	public ResponseEntity<PagedResponse<StudentResponse>> list(
			@RequestParam(required = false) UUID schoolId,
			@RequestParam(required = false) StudentStatus status,
			@RequestParam(required = false) Gender gender,
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(studentService.list(schoolId, status, gender, search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a student by id")
	public ResponseEntity<StudentResponse> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(studentService.getById(id));
	}

	@PostMapping
	@Operation(summary = "Create a new student")
	public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentCreateRequest request) {
		StudentResponse created = studentService.create(request);
		return ResponseEntity.created(URI.create("/api/v1/students/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a student's full profile")
	public ResponseEntity<StudentResponse> update(@PathVariable UUID id,
			@Valid @RequestBody StudentUpdateRequest request) {
		return ResponseEntity.ok(studentService.update(id, request));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update a student's profile")
	public ResponseEntity<StudentResponse> patch(@PathVariable UUID id,
			@Valid @RequestBody StudentPatchRequest request) {
		return ResponseEntity.ok(studentService.patch(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Archive (soft delete) a student")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		studentService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

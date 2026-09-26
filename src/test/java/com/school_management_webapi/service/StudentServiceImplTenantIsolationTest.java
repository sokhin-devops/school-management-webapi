package com.school_management_webapi.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.school_management_webapi.dto.request.StudentCreateRequest;
import com.school_management_webapi.entity.Gender;
import com.school_management_webapi.entity.Student;
import com.school_management_webapi.entity.StudentStatus;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.repository.SchoolRepository;
import com.school_management_webapi.repository.StudentRepository;

/** Tenant A must never reach the students of tenant B. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentServiceImplTenantIsolationTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID TENANT_ID = UUID.randomUUID();
	private static final UUID OWN_SCHOOL_ID = UUID.randomUUID();
	private static final UUID FOREIGN_SCHOOL_ID = UUID.randomUUID();

	@Mock
	private StudentRepository studentRepository;
	@Mock
	private SchoolRepository schoolRepository;
	@Mock
	private TenantAuthorizationService tenantAuthorizationService;
	@Mock
	private SubscriptionLimitService subscriptionLimitService;
	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private StudentServiceImpl studentService;

	@BeforeEach
	void setUp() {
		when(tenantAuthorizationService.requireTenantId(USER_ID)).thenReturn(TENANT_ID);
		when(schoolRepository.existsByIdAndTenantId(OWN_SCHOOL_ID, TENANT_ID)).thenReturn(true);
		when(schoolRepository.existsByIdAndTenantId(FOREIGN_SCHOOL_ID, TENANT_ID)).thenReturn(false);
		when(schoolRepository.findIdsByTenantId(TENANT_ID)).thenReturn(List.of(OWN_SCHOOL_ID));
		when(studentRepository.saveAndFlush(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void readingAStudentOfAnotherTenantIsANotFound() {
		Student foreign = student(FOREIGN_SCHOOL_ID);
		when(studentRepository.findById(foreign.getId())).thenReturn(Optional.of(foreign));

		assertThatThrownBy(() -> studentService.getById(USER_ID, foreign.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deletingAStudentOfAnotherTenantIsANotFound() {
		Student foreign = student(FOREIGN_SCHOOL_ID);
		when(studentRepository.findById(foreign.getId())).thenReturn(Optional.of(foreign));

		assertThatThrownBy(() -> studentService.delete(USER_ID, foreign.getId()))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(studentRepository, never()).delete(any(Student.class));
	}

	@Test
	void creatingAStudentInAForeignSchoolIsRejected() {
		assertThatThrownBy(() -> studentService.create(USER_ID, createRequest(FOREIGN_SCHOOL_ID)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("School not found");

		verify(studentRepository, never()).saveAndFlush(any(Student.class));
	}

	@Test
	void creatingAStudentChecksThePlanStudentLimit() {
		when(studentRepository.countBySchoolIdIn(List.of(OWN_SCHOOL_ID))).thenReturn(42L);

		studentService.create(USER_ID, createRequest(OWN_SCHOOL_ID));

		verify(subscriptionLimitService).checkLimit(TENANT_ID, LimitType.STUDENTS, 42L);
	}

	@Test
	void creatingAStudentStopsAtThePlanStudentLimit() {
		org.mockito.Mockito.doThrow(new ApiException(org.springframework.http.HttpStatus.FORBIDDEN,
				"STUDENT_LIMIT_REACHED", "Student limit reached for your current subscription."))
				.when(subscriptionLimitService).checkLimit(eq(TENANT_ID), eq(LimitType.STUDENTS), anyLong());

		assertThatThrownBy(() -> studentService.create(USER_ID, createRequest(OWN_SCHOOL_ID)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Student limit reached");

		verify(studentRepository, never()).saveAndFlush(any(Student.class));
	}

	private StudentCreateRequest createRequest(UUID schoolId) {
		// branchId and classGroupId are null: a student can exist before they are
		// placed, and this test is about tenant isolation rather than placement.
		return new StudentCreateRequest(schoolId, null, null, "S001", "Dara", null, "Sok", null, Gender.MALE,
				LocalDate.of(2015, 4, 3), null, null, null, null, null, LocalDate.of(2026, 1, 15));
	}

	private Student student(UUID schoolId) {
		return Student.builder()
				.id(UUID.randomUUID())
				.schoolId(schoolId)
				.studentCode("S001")
				.firstName("Dara")
				.lastName("Sok")
				.gender(Gender.MALE)
				.dateOfBirth(LocalDate.of(2015, 4, 3))
				.admissionDate(LocalDate.of(2026, 1, 15))
				.status(StudentStatus.ACTIVE)
				.build();
	}
}

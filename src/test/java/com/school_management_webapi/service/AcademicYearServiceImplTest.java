package com.school_management_webapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.school_management_webapi.dto.request.AcademicYearRequest;
import com.school_management_webapi.entity.AcademicYear;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.entity.SchoolStatus;
import com.school_management_webapi.entity.SchoolType;
import com.school_management_webapi.entity.Tenant;
import com.school_management_webapi.entity.TenantStatus;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.AcademicYearRepository;
import com.school_management_webapi.repository.SchoolRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AcademicYearServiceImplTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID TENANT_ID = UUID.randomUUID();
	private static final LocalDate START = LocalDate.of(2026, 9, 1);
	private static final LocalDate END = LocalDate.of(2027, 6, 30);

	@Mock
	private AcademicYearRepository academicYearRepository;
	@Mock
	private SchoolRepository schoolRepository;
	@Mock
	private TenantAuthorizationService tenantAuthorizationService;
	@Mock
	private CurrentSubscriptionResolver currentSubscriptionResolver;

	@InjectMocks
	private AcademicYearServiceImpl academicYearService;

	private School school;

	@BeforeEach
	void setUp() {
		school = school();
		when(tenantAuthorizationService.requireTenantId(USER_ID)).thenReturn(TENANT_ID);
		when(schoolRepository.findByIdAndTenantId(school.getId(), TENANT_ID)).thenReturn(Optional.of(school));
		when(academicYearRepository.saveAndFlush(any(AcademicYear.class))).thenAnswer(inv -> inv.getArgument(0));
		when(academicYearRepository.findOverlapping(eq(school.getId()), any(), any(), any())).thenReturn(List.of());
	}

	@Test
	void createRejectsAnEndDateThatIsNotAfterTheStartDate() {
		assertThatThrownBy(() -> academicYearService.create(USER_ID, request("2026-2027", END, START)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("endDate must be after startDate");

		verify(academicYearRepository, never()).saveAndFlush(any());
	}

	@Test
	void createRejectsAnOverlappingDateRange() {
		AcademicYear existing = academicYear("2026-2027", START, END, true);
		when(academicYearRepository.findOverlapping(eq(school.getId()), any(), any(), any()))
				.thenReturn(List.of(existing));

		assertThatThrownBy(() -> academicYearService.create(USER_ID,
				request("2027-2028", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31))))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("overlaps academic year '2026-2027'");
	}

	@Test
	void createRejectsADuplicateNameWithinTheSameSchool() {
		when(academicYearRepository.findBySchoolIdAndNameIgnoreCase(school.getId(), "2026-2027"))
				.thenReturn(Optional.of(academicYear("2026-2027", START, END, true)));

		assertThatThrownBy(() -> academicYearService.create(USER_ID, request("2026-2027", START, END)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("already exists");
	}

	@Test
	void createMakesTheFirstYearCurrentEvenWhenNotRequested() {
		when(academicYearRepository.existsBySchoolId(school.getId())).thenReturn(false);

		assertThat(academicYearService.create(USER_ID, request("2026-2027", START, END)).current()).isTrue();
	}

	@Test
	void createRequiresALiveSubscription() {
		when(currentSubscriptionResolver.resolveActive(TENANT_ID)).thenThrow(new ApiException(
				org.springframework.http.HttpStatus.FORBIDDEN, "SUBSCRIPTION_REQUIRED",
				"Tenant has no active subscription"));

		assertThatThrownBy(() -> academicYearService.create(USER_ID, request("2026-2027", START, END)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("no active subscription");
	}

	@Test
	void updateRefusesToLeaveTheSchoolWithoutACurrentYear() {
		AcademicYear current = academicYear("2026-2027", START, END, true);
		when(academicYearRepository.findByIdAndTenantId(current.getId(), TENANT_ID)).thenReturn(Optional.of(current));

		AcademicYearRequest request = new AcademicYearRequest(school.getId(), "2026-2027", START, END, false);

		assertThatThrownBy(() -> academicYearService.update(USER_ID, current.getId(), request))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("must have one current academic year");
	}

	@Test
	void deletePromotesAnotherYearWhenTheCurrentOneGoes() {
		AcademicYear current = academicYear("2026-2027", START, END, true);
		AcademicYear previous = academicYear("2025-2026", START.minusYears(1), END.minusYears(1), false);
		when(academicYearRepository.findByIdAndTenantId(current.getId(), TENANT_ID)).thenReturn(Optional.of(current));
		when(academicYearRepository.findBySchoolIdOrderByStartDateDesc(school.getId()))
				.thenReturn(List.of(current, previous));

		academicYearService.delete(USER_ID, current.getId());

		verify(academicYearRepository).delete(current);
		assertThat(previous.isCurrent()).isTrue();
	}

	private AcademicYearRequest request(String name, LocalDate start, LocalDate end) {
		return new AcademicYearRequest(school.getId(), name, start, end, false);
	}

	private School school() {
		return School.builder()
				.id(UUID.randomUUID())
				.tenant(Tenant.builder().id(TENANT_ID).name("Sunrise").status(TenantStatus.ACTIVE).build())
				.name("Sunrise Academy")
				.type(SchoolType.PRIMARY_SCHOOL)
				.email("hello@sunrise.test")
				.phone("099123456")
				.address("12 Main Street")
				.status(SchoolStatus.ACTIVE)
				.build();
	}

	private AcademicYear academicYear(String name, LocalDate start, LocalDate end, boolean current) {
		return AcademicYear.builder()
				.id(UUID.randomUUID())
				.school(school)
				.name(name)
				.startDate(start)
				.endDate(end)
				.current(current)
				.build();
	}
}

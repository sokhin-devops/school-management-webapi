package com.school_management_webapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import com.school_management_webapi.dto.request.AcademicYearRequest;
import com.school_management_webapi.dto.request.BranchRequest;
import com.school_management_webapi.dto.request.OnboardingAcademicYearRequest;
import com.school_management_webapi.dto.request.OnboardingBranchRequest;
import com.school_management_webapi.dto.request.SchoolRequest;
import com.school_management_webapi.dto.response.OnboardingStatusResponse;
import com.school_management_webapi.dto.response.OnboardingStep;
import com.school_management_webapi.entity.AcademicYear;
import com.school_management_webapi.entity.Branch;
import com.school_management_webapi.entity.BranchStatus;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.entity.SchoolStatus;
import com.school_management_webapi.entity.SchoolType;
import com.school_management_webapi.entity.Tenant;
import com.school_management_webapi.entity.TenantStatus;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.AcademicYearRepository;
import com.school_management_webapi.repository.BranchRepository;
import com.school_management_webapi.repository.SchoolRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OnboardingServiceImplTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID TENANT_ID = UUID.randomUUID();

	@Mock
	private TenantAuthorizationService tenantAuthorizationService;
	@Mock
	private CurrentSubscriptionResolver currentSubscriptionResolver;
	@Mock
	private SchoolRepository schoolRepository;
	@Mock
	private BranchRepository branchRepository;
	@Mock
	private AcademicYearRepository academicYearRepository;
	@Mock
	private SchoolService schoolService;
	@Mock
	private BranchService branchService;
	@Mock
	private AcademicYearService academicYearService;

	@InjectMocks
	private OnboardingServiceImpl onboardingService;

	private School school;

	@BeforeEach
	void setUp() {
		when(tenantAuthorizationService.requireTenantId(USER_ID)).thenReturn(TENANT_ID);
		school = school();
	}

	@Test
	void statusPointsAtSubscriptionStepWhenNoPlanChosen() {
		when(currentSubscriptionResolver.hasActiveSubscription(TENANT_ID)).thenReturn(false);
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.empty());

		OnboardingStatusResponse status = onboardingService.getStatus(USER_ID);

		assertThat(status.currentStep()).isEqualTo(OnboardingStep.SUBSCRIPTION);
		assertThat(status.subscriptionCompleted()).isFalse();
		assertThat(status.completed()).isFalse();
	}

	@Test
	void statusPointsAtBranchStepWhenOnlySchoolExists() {
		when(currentSubscriptionResolver.hasActiveSubscription(TENANT_ID)).thenReturn(true);
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.of(school));
		when(branchRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId())).thenReturn(Optional.empty());
		when(academicYearRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId()))
				.thenReturn(Optional.empty());

		OnboardingStatusResponse status = onboardingService.getStatus(USER_ID);

		assertThat(status.currentStep()).isEqualTo(OnboardingStep.BRANCH);
		assertThat(status.schoolCompleted()).isTrue();
		assertThat(status.school()).isNotNull();
		assertThat(status.branch()).isNull();
	}

	@Test
	void statusIsCompleteWhenAllThreeStepsAnswered() {
		when(currentSubscriptionResolver.hasActiveSubscription(TENANT_ID)).thenReturn(true);
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.of(school));
		when(branchRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId()))
				.thenReturn(Optional.of(branch()));
		when(academicYearRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId()))
				.thenReturn(Optional.of(academicYear()));

		OnboardingStatusResponse status = onboardingService.getStatus(USER_ID);

		assertThat(status.currentStep()).isEqualTo(OnboardingStep.COMPLETE);
		assertThat(status.completed()).isTrue();
	}

	@Test
	void schoolStepRequiresALiveSubscription() {
		when(currentSubscriptionResolver.resolveActive(TENANT_ID)).thenThrow(
				new ApiException(HttpStatus.FORBIDDEN, "SUBSCRIPTION_REQUIRED", "Tenant has no active subscription"));

		assertThatThrownBy(() -> onboardingService.saveSchool(USER_ID, schoolRequest()))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("no active subscription");

		verify(schoolService, never()).create(any(), any());
	}

	@Test
	void schoolStepCreatesOnFirstSubmission() {
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.empty());

		onboardingService.saveSchool(USER_ID, schoolRequest());

		verify(schoolService).create(eq(USER_ID), any(SchoolRequest.class));
		verify(schoolService, never()).update(any(), any(), any());
	}

	@Test
	void schoolStepUpdatesInsteadOfDuplicatingWhenResubmitted() {
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.of(school));

		var result = onboardingService.saveSchool(USER_ID, schoolRequest());

		assertThat(result.created()).isFalse();
		verify(schoolService).update(eq(USER_ID), eq(school.getId()), any(SchoolRequest.class));
		verify(schoolService, never()).create(any(), any());
	}

	@Test
	void branchStepFailsBeforeTheSchoolStep() {
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> onboardingService.saveBranch(USER_ID,
				new OnboardingBranchRequest("Main", "1 Road", "099")))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Complete school setup");
	}

	@Test
	void branchStepMarksTheFirstBranchAsMain() {
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.of(school));
		when(branchRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId())).thenReturn(Optional.empty());

		onboardingService.saveBranch(USER_ID, new OnboardingBranchRequest("Main", "1 Road", "099"));

		ArgumentCaptor<BranchRequest> captor = ArgumentCaptor.forClass(BranchRequest.class);
		verify(branchService).create(eq(USER_ID), captor.capture());
		assertThat(captor.getValue().mainBranch()).isTrue();
		assertThat(captor.getValue().schoolId()).isEqualTo(school.getId());
	}

	@Test
	void branchStepUpdatesTheExistingBranchWhenResubmitted() {
		Branch existing = branch();
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.of(school));
		when(branchRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId())).thenReturn(Optional.of(existing));

		var result = onboardingService.saveBranch(USER_ID, new OnboardingBranchRequest("Renamed", "2 Road", null));

		assertThat(result.created()).isFalse();
		verify(branchService).update(eq(USER_ID), eq(existing.getId()), any(BranchRequest.class));
		verify(branchService, never()).create(any(), any());
	}

	@Test
	void academicYearStepFailsBeforeTheBranchStep() {
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.of(school));
		when(branchRepository.existsBySchoolId(school.getId())).thenReturn(false);

		assertThatThrownBy(() -> onboardingService.saveAcademicYear(USER_ID, new OnboardingAcademicYearRequest(
				"2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30))))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Complete branch setup");
	}

	@Test
	void academicYearStepMarksTheFirstYearAsCurrent() {
		when(schoolRepository.findFirstByTenantIdOrderByCreatedAtAsc(TENANT_ID)).thenReturn(Optional.of(school));
		when(branchRepository.existsBySchoolId(school.getId())).thenReturn(true);
		when(academicYearRepository.findFirstBySchoolIdOrderByCreatedAtAsc(school.getId()))
				.thenReturn(Optional.empty());

		onboardingService.saveAcademicYear(USER_ID, new OnboardingAcademicYearRequest("2026-2027",
				LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30)));

		ArgumentCaptor<AcademicYearRequest> captor = ArgumentCaptor.forClass(AcademicYearRequest.class);
		verify(academicYearService).create(eq(USER_ID), captor.capture());
		assertThat(captor.getValue().current()).isTrue();
		assertThat(captor.getValue().schoolId()).isEqualTo(school.getId());
	}

	private SchoolRequest schoolRequest() {
		return new SchoolRequest("Sunrise Academy", SchoolType.PRIMARY, "hello@sunrise.test", "099123456",
				"12 Main Street");
	}

	private School school() {
		return School.builder()
				.id(UUID.randomUUID())
				.tenant(Tenant.builder().id(TENANT_ID).name("Sunrise").status(TenantStatus.ACTIVE).build())
				.name("Sunrise Academy")
				.type(SchoolType.PRIMARY)
				.email("hello@sunrise.test")
				.phone("099123456")
				.address("12 Main Street")
				.status(SchoolStatus.ACTIVE)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
	}

	private Branch branch() {
		return Branch.builder()
				.id(UUID.randomUUID())
				.school(school)
				.name("Main Campus")
				.address("12 Main Street")
				.mainBranch(true)
				.status(BranchStatus.ACTIVE)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
	}

	private AcademicYear academicYear() {
		return AcademicYear.builder()
				.id(UUID.randomUUID())
				.school(school)
				.name("2026-2027")
				.startDate(LocalDate.of(2026, 9, 1))
				.endDate(LocalDate.of(2027, 6, 30))
				.current(true)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
	}
}

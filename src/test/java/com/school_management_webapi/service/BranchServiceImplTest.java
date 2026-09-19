package com.school_management_webapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.springframework.http.HttpStatus;

import com.school_management_webapi.dto.request.BranchRequest;
import com.school_management_webapi.entity.Branch;
import com.school_management_webapi.entity.BranchStatus;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.entity.SchoolStatus;
import com.school_management_webapi.entity.SchoolType;
import com.school_management_webapi.entity.Tenant;
import com.school_management_webapi.entity.TenantStatus;
import com.school_management_webapi.exception.ApiException;
import com.school_management_webapi.repository.BranchRepository;
import com.school_management_webapi.repository.SchoolRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BranchServiceImplTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID TENANT_ID = UUID.randomUUID();

	@Mock
	private BranchRepository branchRepository;
	@Mock
	private SchoolRepository schoolRepository;
	@Mock
	private TenantAuthorizationService tenantAuthorizationService;
	@Mock
	private SubscriptionLimitService subscriptionLimitService;

	@InjectMocks
	private BranchServiceImpl branchService;

	private School school;

	@BeforeEach
	void setUp() {
		school = school();
		when(tenantAuthorizationService.requireTenantId(USER_ID)).thenReturn(TENANT_ID);
		when(schoolRepository.findByIdAndTenantId(school.getId(), TENANT_ID)).thenReturn(Optional.of(school));
		when(branchRepository.saveAndFlush(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void createChecksTheBranchLimitAgainstTheTenantWideCount() {
		when(branchRepository.countByTenantId(TENANT_ID)).thenReturn(3L);
		when(branchRepository.findBySchoolIdAndNameIgnoreCase(school.getId(), "North")).thenReturn(Optional.empty());

		branchService.create(USER_ID, request("North", true));

		verify(subscriptionLimitService).checkLimit(TENANT_ID, LimitType.BRANCHES, 3L);
	}

	@Test
	void createIsRejectedWhenThePlanBranchLimitIsReached() {
		doThrow(new ApiException(HttpStatus.FORBIDDEN, "BRANCH_LIMIT_REACHED",
				"Branch limit reached for your current subscription."))
				.when(subscriptionLimitService).checkLimit(eq(TENANT_ID), eq(LimitType.BRANCHES), anyLong());

		assertThatThrownBy(() -> branchService.create(USER_ID, request("North", true)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("Branch limit reached");

		verify(branchRepository, never()).saveAndFlush(any());
	}

	@Test
	void createRejectsADuplicateBranchNameWithinTheSameSchool() {
		when(branchRepository.findBySchoolIdAndNameIgnoreCase(school.getId(), "Main Campus"))
				.thenReturn(Optional.of(branch("Main Campus", true)));

		assertThatThrownBy(() -> branchService.create(USER_ID, request("Main Campus", false)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("already exists");
	}

	@Test
	void createMakesTheFirstBranchMainEvenWhenNotRequested() {
		when(branchRepository.existsBySchoolId(school.getId())).thenReturn(false);
		when(branchRepository.findBySchoolIdAndNameIgnoreCase(school.getId(), "Only")).thenReturn(Optional.empty());

		assertThat(branchService.create(USER_ID, request("Only", false)).mainBranch()).isTrue();
	}

	@Test
	void createDemotesThePreviousMainBranch() {
		Branch previousMain = branch("Main Campus", true);
		when(branchRepository.existsBySchoolId(school.getId())).thenReturn(true);
		when(branchRepository.findBySchoolIdAndNameIgnoreCase(school.getId(), "North")).thenReturn(Optional.empty());
		when(branchRepository.findBySchoolIdOrderByCreatedAtAsc(school.getId())).thenReturn(List.of(previousMain));

		assertThat(branchService.create(USER_ID, request("North", true)).mainBranch()).isTrue();
		assertThat(previousMain.isMainBranch()).isFalse();
	}

	@Test
	void updateRefusesToLeaveTheSchoolWithoutAMainBranch() {
		Branch main = branch("Main Campus", true);
		when(branchRepository.findByIdAndTenantId(main.getId(), TENANT_ID)).thenReturn(Optional.of(main));
		when(branchRepository.findBySchoolIdAndNameIgnoreCase(school.getId(), "Main Campus"))
				.thenReturn(Optional.of(main));

		assertThatThrownBy(() -> branchService.update(USER_ID, main.getId(), request("Main Campus", false)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("must have one main branch");
	}

	@Test
	void deletePromotesAnotherBranchWhenTheMainOneGoes() {
		Branch main = branch("Main Campus", true);
		Branch other = branch("North", false);
		when(branchRepository.findByIdAndTenantId(main.getId(), TENANT_ID)).thenReturn(Optional.of(main));
		when(branchRepository.findBySchoolIdOrderByCreatedAtAsc(school.getId())).thenReturn(List.of(main, other));

		branchService.delete(USER_ID, main.getId());

		verify(branchRepository).delete(main);
		assertThat(other.isMainBranch()).isTrue();
	}

	private BranchRequest request(String name, boolean mainBranch) {
		return new BranchRequest(school.getId(), name, "12 Main Street", " 099123456 ", mainBranch);
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
				.build();
	}

	private Branch branch(String name, boolean mainBranch) {
		return Branch.builder()
				.id(UUID.randomUUID())
				.school(school)
				.name(name)
				.address("12 Main Street")
				.mainBranch(mainBranch)
				.status(BranchStatus.ACTIVE)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
	}
}

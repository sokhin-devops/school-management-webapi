package com.school_management_webapi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.response.DashboardSummaryResponse;
import com.school_management_webapi.dto.response.DashboardSummaryResponse.AttendancePoint;
import com.school_management_webapi.dto.response.DashboardSummaryResponse.AttendanceSummary;
import com.school_management_webapi.dto.response.DashboardSummaryResponse.CollectionPoint;
import com.school_management_webapi.entity.AttendanceStatus;
import com.school_management_webapi.entity.ExpenseStatus;
import com.school_management_webapi.entity.PaymentStatus;
import com.school_management_webapi.repository.AttendanceRecordRepository;
import com.school_management_webapi.repository.ClassGroupRepository;
import com.school_management_webapi.repository.ExpenseRepository;
import com.school_management_webapi.repository.ParentRepository;
import com.school_management_webapi.repository.PaymentRepository;
import com.school_management_webapi.repository.StudentRepository;
import com.school_management_webapi.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;

/**
 * The dashboard, counted in the database.
 *
 * Every number here is an aggregate over rows the caller is allowed to see, so
 * the branch list is resolved once and every query is bounded by it. A tenant
 * with no branches gets zeroes rather than an error - that is the honest answer
 * for a school that has not finished onboarding.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

	/** Two working weeks: long enough to show a trend, short enough to read. */
	private static final int TREND_DAYS = 14;

	private static final int TREND_MONTHS = 6;

	private final StudentRepository studentRepository;
	private final TeacherRepository teacherRepository;
	private final ParentRepository parentRepository;
	private final ClassGroupRepository classGroupRepository;
	private final AttendanceRecordRepository attendanceRecordRepository;
	private final PaymentRepository paymentRepository;
	private final ExpenseRepository expenseRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public DashboardSummaryResponse summary(UUID userId, UUID branchId) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		List<UUID> branchIds = resolveBranches(tenantId, branchId);

		if (branchIds.isEmpty()) {
			return empty();
		}

		LocalDate today = LocalDate.now();
		YearMonth thisMonth = YearMonth.from(today);

		return new DashboardSummaryResponse(
				studentRepository.countByBranchIdIn(branchIds),
				teacherRepository.countByBranchIdIn(branchIds),
				parentRepository.countByBranchIdIn(branchIds),
				classGroupRepository.countByBranchIdIn(branchIds),
				attendanceOn(branchIds, today),
				collected(branchIds, thisMonth),
				spent(branchIds, thisMonth),
				attendanceTrend(branchIds, today),
				collectionTrend(branchIds, thisMonth));
	}

	private List<UUID> resolveBranches(UUID tenantId, UUID branchId) {
		if (branchId == null) {
			return branchScopeService.allowedBranchIds(tenantId);
		}
		branchScopeService.requireBranchInTenant(branchId, tenantId);
		return List.of(branchId);
	}

	private AttendanceSummary attendanceOn(List<UUID> branchIds, LocalDate date) {
		List<AttendancePoint> points = attendanceTrendBetween(branchIds, date, date);
		if (points.isEmpty()) {
			return new AttendanceSummary(0, 0, null);
		}
		AttendancePoint point = points.get(0);
		return new AttendanceSummary(point.present(), point.marked(), point.percent());
	}

	private List<AttendancePoint> attendanceTrend(List<UUID> branchIds, LocalDate today) {
		return attendanceTrendBetween(branchIds, today.minusDays(TREND_DAYS - 1L), today);
	}

	/**
	 * Days with no register are left out rather than reported as zero percent: a
	 * weekend nobody marked is not a day everybody missed.
	 */
	private List<AttendancePoint> attendanceTrendBetween(List<UUID> branchIds, LocalDate from, LocalDate to) {
		List<AttendancePoint> points = new ArrayList<>();
		for (Object[] row : attendanceRecordRepository.summariseByDay(branchIds, from, to, AttendanceStatus.PRESENT)) {
			LocalDate date = (LocalDate) row[0];
			long present = ((Number) row[1]).longValue();
			long marked = ((Number) row[2]).longValue();
			points.add(new AttendancePoint(date, present, marked, percentOf(present, marked)));
		}
		return points;
	}

	private List<CollectionPoint> collectionTrend(List<UUID> branchIds, YearMonth thisMonth) {
		List<CollectionPoint> points = new ArrayList<>();
		for (int back = TREND_MONTHS - 1; back >= 0; back--) {
			YearMonth month = thisMonth.minusMonths(back);
			points.add(new CollectionPoint(month.toString(), collected(branchIds, month), spent(branchIds, month)));
		}
		return points;
	}

	private BigDecimal collected(List<UUID> branchIds, YearMonth month) {
		BigDecimal total = paymentRepository.sumAmount(branchIds, PaymentStatus.PAID,
				month.atDay(1), month.atEndOfMonth());
		return total == null ? BigDecimal.ZERO : total;
	}

	private BigDecimal spent(List<UUID> branchIds, YearMonth month) {
		BigDecimal total = expenseRepository.sumAmount(branchIds, ExpenseStatus.PAID,
				month.atDay(1), month.atEndOfMonth());
		return total == null ? BigDecimal.ZERO : total;
	}

	private Double percentOf(long present, long marked) {
		if (marked == 0) {
			return null;
		}
		return Math.round(present * 1000.0 / marked) / 10.0;
	}

	private DashboardSummaryResponse empty() {
		return new DashboardSummaryResponse(0, 0, 0, 0,
				new AttendanceSummary(0, 0, null),
				BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
	}
}

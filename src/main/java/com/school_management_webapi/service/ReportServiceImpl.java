package com.school_management_webapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.response.ReportResponse.AcademicReport;
import com.school_management_webapi.dto.response.ReportResponse.AcademicRow;
import com.school_management_webapi.dto.response.ReportResponse.AttendanceReport;
import com.school_management_webapi.dto.response.ReportResponse.AttendanceRow;
import com.school_management_webapi.dto.response.ReportResponse.EnrolmentReport;
import com.school_management_webapi.dto.response.ReportResponse.EnrolmentRow;
import com.school_management_webapi.dto.response.ReportResponse.FinancialReport;
import com.school_management_webapi.dto.response.ReportResponse.FinancialRow;
import com.school_management_webapi.dto.response.ReportResponse.LedgerRow;
import com.school_management_webapi.entity.Assessment;
import com.school_management_webapi.entity.AttendanceRecord;
import com.school_management_webapi.entity.AttendanceStatus;
import com.school_management_webapi.entity.ClassGroup;
import com.school_management_webapi.entity.Expense;
import com.school_management_webapi.entity.ExpenseStatus;
import com.school_management_webapi.entity.Fee;
import com.school_management_webapi.entity.Payment;
import com.school_management_webapi.entity.PaymentStatus;
import com.school_management_webapi.entity.Student;
import com.school_management_webapi.entity.StudentStatus;
import com.school_management_webapi.entity.Subject;
import com.school_management_webapi.repository.AssessmentRepository;
import com.school_management_webapi.repository.AssessmentScoreRepository;
import com.school_management_webapi.repository.AttendanceRecordRepository;
import com.school_management_webapi.repository.ClassGroupRepository;
import com.school_management_webapi.repository.ExpenseRepository;
import com.school_management_webapi.repository.FeeRepository;
import com.school_management_webapi.repository.PaymentRepository;
import com.school_management_webapi.repository.StudentRepository;
import com.school_management_webapi.repository.SubjectRepository;

import lombok.RequiredArgsConstructor;

/**
 * 50-reports.md: one framework, four templates.
 *
 * Every report is a roll-up of rows the caller is already allowed to see, so the
 * branch list is resolved once and every query is bounded by it. A branch with
 * nothing in it returns no rows rather than an error - that is the honest answer
 * for a school that has not started yet.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

	private static final int LEDGER_MONTHS = 6;

	private final ClassGroupRepository classGroupRepository;
	private final StudentRepository studentRepository;
	private final AttendanceRecordRepository attendanceRecordRepository;
	private final AssessmentRepository assessmentRepository;
	private final AssessmentScoreRepository assessmentScoreRepository;
	private final SubjectRepository subjectRepository;
	private final FeeRepository feeRepository;
	private final PaymentRepository paymentRepository;
	private final ExpenseRepository expenseRepository;
	private final BranchScopeService branchScopeService;
	private final TenantSettingsService tenantSettingsService;

	@Override
	public EnrolmentReport enrolment(UUID userId, UUID branchId) {
		List<UUID> branchIds = resolve(userId, branchId);
		if (branchIds.isEmpty()) {
			return new EnrolmentReport(List.of());
		}

		List<Student> students = studentRepository.findByBranchIdIn(branchIds);
		Map<UUID, List<Student>> byClass = students.stream()
				.filter(student -> student.getClassGroupId() != null)
				.collect(Collectors.groupingBy(Student::getClassGroupId));

		List<EnrolmentRow> rows = classGroupRepository.findByBranchIdIn(branchIds).stream()
				.sorted(Comparator.comparing(ClassGroup::getName))
				.map(group -> {
					List<Student> roll = byClass.getOrDefault(group.getId(), List.of());
					long active = roll.stream().filter(s -> s.getStatus() == StudentStatus.ACTIVE).count();
					return new EnrolmentRow(group.getName(), roll.size(), group.getCapacity(), active,
							roll.size() - active);
				})
				.toList();

		return new EnrolmentReport(rows);
	}

	@Override
	public AttendanceReport attendance(UUID userId, UUID branchId, LocalDate from, LocalDate to) {
		List<UUID> branchIds = resolve(userId, branchId);
		if (branchIds.isEmpty()) {
			return new AttendanceReport(List.of());
		}

		LocalDate start = from != null ? from : LocalDate.now().minusMonths(1);
		LocalDate end = to != null ? to : LocalDate.now();

		List<AttendanceRecord> records = attendanceRecordRepository
				.findByBranchIdInAndAttendanceDateBetween(branchIds, start, end);
		Map<UUID, String> classNames = classGroupRepository.findByBranchIdIn(branchIds).stream()
				.collect(Collectors.toMap(ClassGroup::getId, ClassGroup::getName));

		List<AttendanceRow> rows = records.stream()
				.collect(Collectors.groupingBy(AttendanceRecord::getClassGroupId))
				.entrySet().stream()
				.map(entry -> {
					List<AttendanceRecord> marks = entry.getValue();
					// One sitting is one date-and-session, however many students it covered.
					long sessions = marks.stream()
							.map(record -> record.getAttendanceDate() + "|" + record.getSession())
							.distinct().count();
					return new AttendanceRow(
							classNames.getOrDefault(entry.getKey(), "Unknown class"),
							sessions,
							count(marks, AttendanceStatus.PRESENT),
							count(marks, AttendanceStatus.ABSENT),
							count(marks, AttendanceStatus.LATE),
							count(marks, AttendanceStatus.EXCUSED));
				})
				.sorted(Comparator.comparing(AttendanceRow::className))
				.toList();

		return new AttendanceReport(rows);
	}

	@Override
	public AcademicReport academic(UUID userId, UUID branchId) {
		List<UUID> branchIds = resolve(userId, branchId);
		if (branchIds.isEmpty()) {
			return new AcademicReport(List.of());
		}

		Map<UUID, String> subjectNames = subjectRepository.findByBranchIdIn(branchIds).stream()
				.collect(Collectors.toMap(Subject::getId, Subject::getName));

		// 63-academic-settings.md: the school sets its own pass mark.
		BigDecimal passShare = BigDecimal.valueOf(
				tenantSettingsService.academicFor(branchScopeService.requireTenantId(userId)).getPassMark())
				.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

		List<AcademicRow> rows = assessmentRepository.findByBranchIdIn(branchIds).stream()
				.collect(Collectors.groupingBy(Assessment::getSubjectId))
				.entrySet().stream()
				.map(entry -> academicRow(subjectNames.getOrDefault(entry.getKey(), "Unknown subject"),
						entry.getValue(), passShare))
				.sorted(Comparator.comparing(AcademicRow::subject))
				.toList();

		return new AcademicReport(rows);
	}

	@Override
	public FinancialReport financial(UUID userId, UUID branchId, LocalDate from, LocalDate to) {
		List<UUID> branchIds = resolve(userId, branchId);
		if (branchIds.isEmpty()) {
			return new FinancialReport(List.of(), List.of());
		}

		LocalDate end = to != null ? to : LocalDate.now();
		LocalDate start = from != null ? from : YearMonth.from(end).minusMonths(LEDGER_MONTHS - 1L).atDay(1);

		Map<UUID, Fee> fees = feeRepository.findByBranchIdIn(branchIds).stream()
				.collect(Collectors.toMap(Fee::getId, Function.identity()));
		List<Payment> payments = paymentRepository.findByBranchIdInAndPaidOnBetween(branchIds, start, end);

		// Invoiced is what the fees in each category are worth, not what has been
		// billed to individual students: nothing issues invoices yet, so the
		// report says what it can stand behind.
		Map<String, BigDecimal> invoiced = fees.values().stream()
				.collect(Collectors.groupingBy(Fee::getCategory,
						Collectors.reducing(BigDecimal.ZERO, Fee::getAmount, BigDecimal::add)));

		Map<String, BigDecimal> collected = payments.stream()
				.filter(payment -> payment.getStatus() == PaymentStatus.PAID)
				.filter(payment -> fees.containsKey(payment.getFeeId()))
				.collect(Collectors.groupingBy(payment -> fees.get(payment.getFeeId()).getCategory(),
						Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

		List<FinancialRow> categories = invoiced.keySet().stream()
				.sorted()
				.map(category -> new FinancialRow(category, invoiced.get(category),
						collected.getOrDefault(category, BigDecimal.ZERO)))
				.toList();

		return new FinancialReport(categories, ledger(branchIds, YearMonth.from(end)));
	}

	private List<LedgerRow> ledger(List<UUID> branchIds, YearMonth lastMonth) {
		List<LedgerRow> rows = new ArrayList<>();
		for (int back = LEDGER_MONTHS - 1; back >= 0; back--) {
			YearMonth month = lastMonth.minusMonths(back);
			BigDecimal collected = orZero(paymentRepository.sumAmount(branchIds, PaymentStatus.PAID,
					month.atDay(1), month.atEndOfMonth()));
			BigDecimal spent = orZero(expenseRepository.sumAmount(branchIds, ExpenseStatus.PAID,
					month.atDay(1), month.atEndOfMonth()));
			rows.add(new LedgerRow(month.toString(), collected, spent));
		}
		return rows;
	}

	/**
	 * Averages are taken across marked papers only. An unmarked one counted as
	 * zero would drag a half-marked subject down and read as a worse term than
	 * it was.
	 */
	private AcademicRow academicRow(String subject, List<Assessment> assessments, BigDecimal passShare) {
		List<BigDecimal> shares = new ArrayList<>();
		BigDecimal highest = null;
		BigDecimal lowest = null;
		long passes = 0;

		for (Assessment assessment : assessments) {
			BigDecimal max = BigDecimal.valueOf(assessment.getMaxScore() == null ? 0 : assessment.getMaxScore());
			if (max.signum() == 0) {
				continue;
			}
			for (var score : assessmentScoreRepository.findByAssessmentIdOrderByCreatedAtAsc(assessment.getId())) {
				if (score.getScore() == null) {
					continue;
				}
				BigDecimal share = score.getScore().divide(max, 4, RoundingMode.HALF_UP);
				shares.add(share);
				highest = highest == null || share.compareTo(highest) > 0 ? share : highest;
				lowest = lowest == null || share.compareTo(lowest) < 0 ? share : lowest;
				if (share.compareTo(passShare) >= 0) {
					passes++;
				}
			}
		}

		if (shares.isEmpty()) {
			return new AcademicRow(subject, assessments.size(), null, null, null, null);
		}

		BigDecimal average = shares.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(BigDecimal.valueOf(shares.size()), 4, RoundingMode.HALF_UP);

		return new AcademicRow(subject, assessments.size(), percent(average), percent(highest), percent(lowest),
				percent(BigDecimal.valueOf(passes).divide(BigDecimal.valueOf(shares.size()), 4, RoundingMode.HALF_UP)));
	}

	/** Scores are held as a share of the maximum and reported out of a hundred. */
	private BigDecimal percent(BigDecimal share) {
		return share == null ? null : share.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
	}

	private BigDecimal orZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private long count(List<AttendanceRecord> records, AttendanceStatus status) {
		return records.stream().filter(record -> record.getStatus() == status).count();
	}

	private List<UUID> resolve(UUID userId, UUID branchId) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId == null) {
			return branchScopeService.allowedBranchIds(tenantId);
		}
		branchScopeService.requireBranchInTenant(branchId, tenantId);
		return List.of(branchId);
	}
}

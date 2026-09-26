package com.school_management_webapi.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.entity.AcademicYear;
import com.school_management_webapi.entity.School;
import com.school_management_webapi.entity.TenantSettings;
import com.school_management_webapi.repository.AcademicYearRepository;
import com.school_management_webapi.repository.SchoolRepository;
import com.school_management_webapi.repository.TenantSettingsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The work nobody asks for in a request: renewing billing periods, and rolling
 * a school into its next academic year when it has asked for that.
 *
 * Both run nightly and are safe to run twice - each only acts on what is due.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledJobs {

	private final SubscriptionBillingService billingService;
	private final TenantSettingsRepository tenantSettingsRepository;
	private final SchoolRepository schoolRepository;
	private final AcademicYearRepository academicYearRepository;
	private final NotificationService notificationService;

	@Scheduled(cron = "0 17 2 * * *")
	public void renewSubscriptions() {
		billingService.renewDue();
	}

	/**
	 * 63-academic-settings.md's auto rollover: once the current year has ended,
	 * the next year that has been set up becomes current. A school with no next
	 * year set up is left where it is - inventing one would be guessing its dates.
	 */
	@Scheduled(cron = "0 23 2 * * *")
	@Transactional
	public void rollOverAcademicYears() {
		LocalDate today = LocalDate.now();
		for (TenantSettings settings : tenantSettingsRepository.findAll()) {
			if (!settings.getAcademic().isAutoRollover()) {
				continue;
			}
			for (School school : schoolRepository.findByTenantIdOrderByCreatedAtAsc(settings.getTenantId())) {
				rollOver(settings.getTenantId(), school, today);
			}
		}
	}

	private void rollOver(UUID tenantId, School school, LocalDate today) {
		List<AcademicYear> years = academicYearRepository.findBySchoolIdOrderByStartDateDesc(school.getId());
		Optional<AcademicYear> current = years.stream().filter(AcademicYear::isCurrent).findFirst();
		if (current.isEmpty() || !current.get().getEndDate().isBefore(today)) {
			return;
		}

		AcademicYear ending = current.get();
		Optional<AcademicYear> next = years.stream()
				.filter(year -> year.getStartDate().isAfter(ending.getStartDate()))
				.min(Comparator.comparing(AcademicYear::getStartDate));
		if (next.isEmpty()) {
			return;
		}

		ending.setCurrent(false);
		next.get().setCurrent(true);
		academicYearRepository.save(ending);
		academicYearRepository.save(next.get());
		log.info("School {} rolled over from {} to {}", school.getId(), ending.getName(), next.get().getName());
		notificationService.notifyTenant(tenantId, NotificationEvent.YEAR_ROLLED_OVER,
				ending.getName() + " has ended; " + next.get().getName() + " is now the current year.",
				"/academic/academic-years");
	}
}

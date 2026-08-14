package com.school_management_webapi.config;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.entity.Plan;
import com.school_management_webapi.entity.PlanFeature;
import com.school_management_webapi.entity.PlanStatus;
import com.school_management_webapi.repository.PlanFeatureRepository;
import com.school_management_webapi.repository.PlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanDataSeeder implements ApplicationRunner {

	private static final Map<String, String> FEATURE_NAMES = Map.ofEntries(
			Map.entry("STUDENT_MANAGEMENT", "Student Management"),
			Map.entry("TEACHER_MANAGEMENT", "Teacher Management"),
			Map.entry("PARENT_MANAGEMENT", "Parent Management"),
			Map.entry("ATTENDANCE", "Attendance"),
			Map.entry("EXAMS", "Exams"),
			Map.entry("GRADES", "Grades"),
			Map.entry("FINANCE", "Finance"),
			Map.entry("REPORTS", "Reports"),
			Map.entry("MULTIPLE_BRANCHES", "Multiple Branches"),
			Map.entry("CUSTOM_ROLES", "Custom Roles"),
			Map.entry("NOTIFICATIONS", "Notifications"));

	private final PlanRepository planRepository;
	private final PlanFeatureRepository planFeatureRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (planRepository.count() > 0) {
			return;
		}

		seedPlan("starter", "Starter", "For small schools", "19.00", "190.00", 300, 30, 1, 1,
				Set.of("STUDENT_MANAGEMENT", "TEACHER_MANAGEMENT", "PARENT_MANAGEMENT", "ATTENDANCE",
						"NOTIFICATIONS"),
				Map.of());

		seedPlan("professional", "Professional", "For growing schools", "49.00", "490.00", 1000, 100, 5, 2,
				Set.of("STUDENT_MANAGEMENT", "TEACHER_MANAGEMENT", "PARENT_MANAGEMENT", "ATTENDANCE", "EXAMS",
						"GRADES", "FINANCE", "REPORTS", "MULTIPLE_BRANCHES", "CUSTOM_ROLES", "NOTIFICATIONS"),
				Map.of("MULTIPLE_BRANCHES", 5));

		seedPlan("enterprise", "Enterprise", "For large institutions and school groups", "199.00", "1990.00",
				null, null, null, 3,
				Set.copyOf(FEATURE_NAMES.keySet()),
				Map.of());

		log.info("Seeded default subscription plans: starter, professional, enterprise");
	}

	private void seedPlan(String code, String name, String description, String priceMonthly, String priceYearly,
			Integer maxStudents, Integer maxTeachers, Integer maxBranches, int sortOrder,
			Set<String> enabledFeatures, Map<String, Integer> limits) {

		Plan plan = planRepository.save(Plan.builder()
				.code(code)
				.name(name)
				.description(description)
				.priceMonthly(new BigDecimal(priceMonthly))
				.priceYearly(new BigDecimal(priceYearly))
				.currency("USD")
				.maxStudents(maxStudents)
				.maxTeachers(maxTeachers)
				.maxBranches(maxBranches)
				.status(PlanStatus.ACTIVE)
				.sortOrder(sortOrder)
				.build());

		FEATURE_NAMES.forEach((featureCode, featureName) -> planFeatureRepository.save(PlanFeature.builder()
				.plan(plan)
				.featureCode(featureCode)
				.featureName(featureName)
				.enabled(enabledFeatures.contains(featureCode))
				.limitValue(limits.get(featureCode))
				.build()));
	}
}

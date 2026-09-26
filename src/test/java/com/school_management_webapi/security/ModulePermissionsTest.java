package com.school_management_webapi.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.school_management_webapi.entity.PermissionAction;

/** The one map every request is checked against: path to module, and to plan feature. */
class ModulePermissionsTest {

	@Test
	void resourcesMapToTheirModules() {
		assertThat(ModulePermissions.moduleFor("/api/v1/students", PermissionAction.VIEW)).contains("students");
		assertThat(ModulePermissions.moduleFor("/api/v1/fees/123", PermissionAction.EDIT)).contains("finance");
		assertThat(ModulePermissions.moduleFor("/api/v1/classes", PermissionAction.CREATE)).contains("academic");
		assertThat(ModulePermissions.moduleFor("/api/v1/subscriptions/current", PermissionAction.EDIT))
				.contains("settings");
	}

	@Test
	void contextAnyoneMayRead() {
		// The branch switcher and the school sit on every page.
		assertThat(ModulePermissions.moduleFor("/api/v1/branches", PermissionAction.VIEW)).isEmpty();
		assertThat(ModulePermissions.moduleFor("/api/v1/schools", PermissionAction.VIEW)).isEmpty();
		// ...but changing them is still Settings.
		assertThat(ModulePermissions.moduleFor("/api/v1/branches", PermissionAction.CREATE)).contains("settings");
	}

	@Test
	void readablePathsAreExactNotPrefixes() {
		assertThat(ModulePermissions.moduleFor("/api/v1/settings/academic", PermissionAction.VIEW)).isEmpty();
		assertThat(ModulePermissions.moduleFor("/api/v1/subscriptions/current", PermissionAction.VIEW)).isEmpty();
		// Reading the plan is open; reading its invoices is not.
		assertThat(ModulePermissions.moduleFor("/api/v1/subscriptions/current/usage", PermissionAction.VIEW))
				.contains("settings");
		assertThat(ModulePermissions.moduleFor("/api/v1/settings/security", PermissionAction.VIEW))
				.contains("settings");
	}

	@Test
	void pathsOutsideTheGridPass() {
		assertThat(ModulePermissions.moduleFor("/api/v1/auth/me", PermissionAction.VIEW)).isEmpty();
		assertThat(ModulePermissions.moduleFor("/api/v1/dashboard/summary", PermissionAction.VIEW)).isEmpty();
		assertThat(ModulePermissions.moduleFor("/api/v1/notifications", PermissionAction.VIEW)).isEmpty();
	}

	@Test
	void resourcesMapToThePlanFeaturesThatSellThem() {
		assertThat(ModulePermissions.featureFor("/api/v1/payments", PermissionAction.VIEW)).contains("FINANCE");
		assertThat(ModulePermissions.featureFor("/api/v1/reports/financial", PermissionAction.VIEW)).contains("REPORTS");
		assertThat(ModulePermissions.featureFor("/api/v1/assessments", PermissionAction.VIEW)).contains("EXAMS");
		assertThat(ModulePermissions.featureFor("/api/v1/assessments/42/scores", PermissionAction.EDIT))
				.contains("GRADES");
	}

	@Test
	void customRolesGateOnlyWriting() {
		// Every user holds a default role, so reading roles is never a plan feature.
		assertThat(ModulePermissions.featureFor("/api/v1/roles", PermissionAction.VIEW)).isEmpty();
		assertThat(ModulePermissions.featureFor("/api/v1/roles", PermissionAction.CREATE)).contains("CUSTOM_ROLES");
	}

	@Test
	void methodsMapToActions() {
		assertThat(ModulePermissions.actionFor("GET")).contains(PermissionAction.VIEW);
		assertThat(ModulePermissions.actionFor("post")).contains(PermissionAction.CREATE);
		assertThat(ModulePermissions.actionFor("PATCH")).contains(PermissionAction.EDIT);
		assertThat(ModulePermissions.actionFor("DELETE")).contains(PermissionAction.DELETE);
		assertThat(ModulePermissions.actionFor("OPTIONS")).isEqualTo(Optional.empty());
	}
}

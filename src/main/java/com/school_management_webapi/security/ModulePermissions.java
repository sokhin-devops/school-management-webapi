package com.school_management_webapi.security;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.school_management_webapi.entity.PermissionAction;

/**
 * Which module a request belongs to, and what it is trying to do.
 *
 * Derived from the path and the HTTP method rather than declared on each
 * controller: twenty controllers annotated by hand is twenty chances to forget
 * one, and a forgotten annotation is an unguarded endpoint.
 */
public final class ModulePermissions {

	/** The modules a role's grid is drawn from - the same list the web app offers. */
	private static final Map<String, String> MODULE_BY_RESOURCE = Map.ofEntries(
			Map.entry("students", "students"),
			Map.entry("teachers", "teachers"),
			Map.entry("parents", "parents"),
			Map.entry("programs", "academic"),
			Map.entry("levels", "academic"),
			Map.entry("subjects", "academic"),
			Map.entry("rooms", "academic"),
			Map.entry("classes", "academic"),
			Map.entry("academic-years", "academic"),
			Map.entry("assessments", "academic"),
			Map.entry("attendance", "attendance"),
			Map.entry("fees", "finance"),
			Map.entry("payments", "finance"),
			Map.entry("expenses", "finance"),
			Map.entry("reports", "reports"),
			Map.entry("roles", "settings"),
			Map.entry("users", "settings"),
			Map.entry("branches", "settings"),
			Map.entry("schools", "settings"),
			Map.entry("settings", "settings"),
			Map.entry("system", "settings"),
			// 68-subscription.md: "Respect the permissions of the account
			// owner/admin." Outside the grid, any member could change or cancel
			// the plan.
			Map.entry("subscriptions", "settings"));

	/**
	 * Resources everyone signed in may read, whatever their grid says.
	 *
	 * The school and its branches are the context every other screen is scoped
	 * to - the branch switcher sits in the topbar of every page. Withholding
	 * them from a teacher would leave them unable to say which branch their own
	 * class list is for. Changing them still belongs to Settings, so only the
	 * reading is let through.
	 */
	private static final Set<String> READABLE_BY_ANYONE = Set.of("branches", "schools");

	/**
	 * Paths under a guarded resource that everyone may read: the academic
	 * settings decide which pages anyone's menu shows, and the current plan is
	 * what onboarding reads before a role could have been assigned.
	 */
	private static final Set<String> READABLE_PATHS = Set.of("settings/academic", "subscriptions/current");

	/** Which plan feature each resource belongs to - the codes the plans are seeded with. */
	private static final Map<String, String> FEATURE_BY_RESOURCE = Map.ofEntries(
			Map.entry("students", "STUDENT_MANAGEMENT"),
			Map.entry("teachers", "TEACHER_MANAGEMENT"),
			Map.entry("parents", "PARENT_MANAGEMENT"),
			Map.entry("attendance", "ATTENDANCE"),
			Map.entry("fees", "FINANCE"),
			Map.entry("payments", "FINANCE"),
			Map.entry("expenses", "FINANCE"),
			Map.entry("reports", "REPORTS"),
			Map.entry("assessments", "EXAMS"));

	private ModulePermissions() {
	}

	/**
	 * The plan feature a request needs, if any. Marks are Grades rather than
	 * Exams; and custom roles only gate making or changing one - the default
	 * roles can always be read, since every user holds one.
	 */
	public static Optional<String> featureFor(String path, PermissionAction action) {
		String[] segments = path.split("/");
		for (int i = 0; i < segments.length; i++) {
			if ("v1".equals(segments[i]) && i + 1 < segments.length) {
				String resource = segments[i + 1];
				if ("roles".equals(resource)) {
					return action == PermissionAction.VIEW ? Optional.empty() : Optional.of("CUSTOM_ROLES");
				}
				if ("assessments".equals(resource) && path.contains("/scores")) {
					return Optional.of("GRADES");
				}
				return Optional.ofNullable(FEATURE_BY_RESOURCE.get(resource));
			}
		}
		return Optional.empty();
	}

	/**
	 * The module a path belongs to, or empty when there is nothing to check -
	 * auth, plans, onboarding, notifications and the dashboard are reachable by
	 * anyone who is signed in, as are reads of the context resources above.
	 */
	public static Optional<String> moduleFor(String path, PermissionAction action) {
		String[] segments = path.split("/");
		for (int i = 0; i < segments.length; i++) {
			if ("v1".equals(segments[i]) && i + 1 < segments.length) {
				String resource = segments[i + 1];
				if (action == PermissionAction.VIEW && READABLE_BY_ANYONE.contains(resource)) {
					return Optional.empty();
				}
				if (action == PermissionAction.VIEW && i + 2 < segments.length
						&& READABLE_PATHS.contains(resource + "/" + segments[i + 2])
						&& i + 3 == segments.length) {
					return Optional.empty();
				}
				return Optional.ofNullable(MODULE_BY_RESOURCE.get(resource));
			}
		}
		return Optional.empty();
	}

	/** Reading is VIEW; the three ways of writing map to the three write actions. */
	public static Optional<PermissionAction> actionFor(String method) {
		return switch (method.toUpperCase()) {
			case "GET", "HEAD" -> Optional.of(PermissionAction.VIEW);
			case "POST" -> Optional.of(PermissionAction.CREATE);
			case "PUT", "PATCH" -> Optional.of(PermissionAction.EDIT);
			case "DELETE" -> Optional.of(PermissionAction.DELETE);
			default -> Optional.empty();
		};
	}
}

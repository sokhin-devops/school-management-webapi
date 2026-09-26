package com.school_management_webapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.school_management_webapi.security.PermissionInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Registers the permission check across the API.
 *
 * Auth, plans, onboarding, notifications and the dashboard are left out of the
 * grid entirely rather than excluded here - a path with no module simply passes
 * - so the one rule is the map in ModulePermissions.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final PermissionInterceptor permissionInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(permissionInterceptor).addPathPatterns("/api/v1/**");
	}
}

package com.school_management_webapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One cell of the permission grid: a module and one thing that may be done to
 * it. A role holds a set of these, so granting view-only on Finance is three
 * absent rows rather than a flag that has to be interpreted.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RolePermission {

	@Column(name = "module", nullable = false, length = 60)
	private String module;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, length = 20)
	private PermissionAction action;
}

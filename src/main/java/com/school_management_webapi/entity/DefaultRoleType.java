package com.school_management_webapi.entity;

/**
 * The five roles every tenant starts with. 64-users-and-roles.md: they cannot be
 * edited or deleted, so the type is stored rather than inferred from the name -
 * a renamed role would otherwise stop being protected.
 */
public enum DefaultRoleType {
	OWNER,
	ACCOUNTING,
	TEACHER,
	PARENT,
	STUDENT
}

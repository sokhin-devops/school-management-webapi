package com.school_management_webapi.entity;

/**
 * What a role may do to a module. 64-users-and-roles.md: the permission tree is
 * a module crossed with these four, which is why they are stored as rows rather
 * than as a bitmask nobody can read in the database.
 */
public enum PermissionAction {
	VIEW,
	CREATE,
	EDIT,
	DELETE
}

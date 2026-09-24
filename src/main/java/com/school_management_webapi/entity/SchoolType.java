package com.school_management_webapi.entity;

/**
 * The kinds of institution the product serves.
 *
 * Kept identical to the set the web app offers (SchoolType in core/models), so
 * the value a user picks is the value that is stored, with no translation table
 * in between to drift.
 *
 * PRIMARY and SECONDARY were the earlier spellings; rows written before this
 * need a one-off
 * {@code UPDATE schools SET type = type || '_SCHOOL' WHERE type IN ('PRIMARY', 'SECONDARY')},
 * because ddl-auto=update does not rewrite stored enum values.
 */
public enum SchoolType {
	PRIMARY_SCHOOL,
	SECONDARY_SCHOOL,
	HIGH_SCHOOL,
	COLLEGE,
	UNIVERSITY,
	LANGUAGE_CENTER,
	TRAINING_CENTER,
	VOCATIONAL_SCHOOL,
	OTHER
}

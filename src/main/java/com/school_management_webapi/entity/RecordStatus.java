package com.school_management_webapi.entity;

/**
 * The plain on/off state most reference data carries. Domains with a real
 * lifecycle of their own - students, payments, expenses - keep their own enum
 * rather than being squeezed into this one.
 */
public enum RecordStatus {
	ACTIVE,
	INACTIVE
}

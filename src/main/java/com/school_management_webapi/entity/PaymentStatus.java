package com.school_management_webapi.entity;

/**
 * Where a payment stands. PARTIALLY_PAID exists because a fee may be
 * settled in instalments, which is the common case for tuition.
 */
public enum PaymentStatus {
	PENDING,
	PAID,
	PARTIALLY_PAID,
	FAILED,
	REFUNDED
}

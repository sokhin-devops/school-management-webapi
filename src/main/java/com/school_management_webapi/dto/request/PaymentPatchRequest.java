package com.school_management_webapi.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.PaymentMethod;
import com.school_management_webapi.entity.PaymentStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

/** A partial change. Every field is optional; the ones left out keep their current value. */
public record PaymentPatchRequest(
		UUID branchId,

		@Size(max = 50, message = "reference must be 50 characters or fewer") String reference,

		UUID feeId,

		UUID studentId,

		@DecimalMin(value = "0.01", message = "amount must be 0.01 or more") BigDecimal amount,

		LocalDate paidOn,

		PaymentMethod method,

		PaymentStatus status,

		@Size(max = 150, message = "payerName must be 150 characters or fewer") String payerName,

		@Size(max = 500, message = "notes must be 500 characters or fewer") String notes) {
}

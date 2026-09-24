package com.school_management_webapi.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.PaymentMethod;
import com.school_management_webapi.entity.PaymentStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A full replacement: every field is written, so every required one must be present. */
public record PaymentUpdateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		@NotBlank(message = "reference is required") @Size(max = 50, message = "reference must be 50 characters or fewer") String reference,

		@NotNull(message = "feeId is required") UUID feeId,

		@NotNull(message = "studentId is required") UUID studentId,

		@NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be 0.01 or more") BigDecimal amount,

		@NotNull(message = "paidOn is required") LocalDate paidOn,

		@NotNull(message = "method is required") PaymentMethod method,

		@NotNull(message = "status is required") PaymentStatus status,

		@Size(max = 150, message = "payerName must be 150 characters or fewer") String payerName,

		@Size(max = 500, message = "notes must be 500 characters or fewer") String notes) {
}

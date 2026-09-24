package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.PaymentMethod;
import com.school_management_webapi.entity.PaymentStatus;

public record PaymentResponse(
		UUID id,
		UUID branchId,
		String reference,
		UUID feeId,
		UUID studentId,
		BigDecimal amount,
		LocalDate paidOn,
		PaymentMethod method,
		PaymentStatus status,
		String payerName,
		String notes,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

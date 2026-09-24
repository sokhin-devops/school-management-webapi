package com.school_management_webapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.school_management_webapi.entity.ExpenseStatus;

public record ExpenseResponse(
		UUID id,
		UUID branchId,
		String description,
		String category,
		BigDecimal amount,
		LocalDate spentOn,
		ExpenseStatus status,
		String attachmentUrl,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}

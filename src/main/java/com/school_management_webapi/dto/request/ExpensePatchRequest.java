package com.school_management_webapi.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.ExpenseStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

/** A partial change. Every field is optional; the ones left out keep their current value. */
public record ExpensePatchRequest(
		UUID branchId,

		@Size(max = 255, message = "description must be 255 characters or fewer") String description,

		@Size(max = 80, message = "category must be 80 characters or fewer") String category,

		@DecimalMin(value = "0.01", message = "amount must be 0.01 or more") BigDecimal amount,

		LocalDate spentOn,

		ExpenseStatus status,

		@Size(max = 500, message = "attachmentUrl must be 500 characters or fewer") String attachmentUrl) {
}

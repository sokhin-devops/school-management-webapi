package com.school_management_webapi.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.school_management_webapi.entity.ExpenseStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The fields needed to open a new record. */
public record ExpenseCreateRequest(
		@NotNull(message = "branchId is required") UUID branchId,

		@NotBlank(message = "description is required") @Size(max = 255, message = "description must be 255 characters or fewer") String description,

		@NotBlank(message = "category is required") @Size(max = 80, message = "category must be 80 characters or fewer") String category,

		@NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be 0.01 or more") BigDecimal amount,

		@NotNull(message = "spentOn is required") LocalDate spentOn,

		@NotNull(message = "status is required") ExpenseStatus status,

		@Size(max = 500, message = "attachmentUrl must be 500 characters or fewer") String attachmentUrl) {
}

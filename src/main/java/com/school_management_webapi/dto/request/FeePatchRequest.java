package com.school_management_webapi.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.school_management_webapi.entity.RecordStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

/** A partial change. Every field is optional; the ones left out keep their current value. */
public record FeePatchRequest(
		UUID branchId,

		UUID academicYearId,

		@Size(max = 150, message = "name must be 150 characters or fewer") String name,

		@Size(max = 80, message = "category must be 80 characters or fewer") String category,

		@DecimalMin(value = "0.01", message = "amount must be 0.01 or more") BigDecimal amount,

		@Size(max = 500, message = "description must be 500 characters or fewer") String description,

		RecordStatus status) {
}

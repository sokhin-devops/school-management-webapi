package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.ExpenseCreateRequest;
import com.school_management_webapi.dto.request.ExpensePatchRequest;
import com.school_management_webapi.dto.request.ExpenseUpdateRequest;
import com.school_management_webapi.dto.response.ExpenseResponse;
import com.school_management_webapi.entity.Expense;

public final class ExpenseMapper {

	private ExpenseMapper() {
	}

	public static Expense toEntity(ExpenseCreateRequest request) {
		return Expense.builder()
				.branchId(request.branchId())
				.description(request.description())
				.category(request.category())
				.amount(request.amount())
				.spentOn(request.spentOn())
				.status(request.status())
				.attachmentUrl(request.attachmentUrl())
				.build();
	}

	public static void updateEntity(Expense entity, ExpenseUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setDescription(request.description());
		entity.setCategory(request.category());
		entity.setAmount(request.amount());
		entity.setSpentOn(request.spentOn());
		entity.setStatus(request.status());
		entity.setAttachmentUrl(request.attachmentUrl());
	}

	public static void patchEntity(Expense entity, ExpensePatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.description() != null) {
			entity.setDescription(request.description());
		}

		if (request.category() != null) {
			entity.setCategory(request.category());
		}

		if (request.amount() != null) {
			entity.setAmount(request.amount());
		}

		if (request.spentOn() != null) {
			entity.setSpentOn(request.spentOn());
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}

		if (request.attachmentUrl() != null) {
			entity.setAttachmentUrl(request.attachmentUrl());
		}
	}

	public static ExpenseResponse toResponse(Expense entity) {
		return new ExpenseResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getDescription(),
				entity.getCategory(),
				entity.getAmount(),
				entity.getSpentOn(),
				entity.getStatus(),
				entity.getAttachmentUrl(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

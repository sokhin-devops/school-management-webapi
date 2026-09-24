package com.school_management_webapi.mapper;

import com.school_management_webapi.dto.request.PaymentCreateRequest;
import com.school_management_webapi.dto.request.PaymentPatchRequest;
import com.school_management_webapi.dto.request.PaymentUpdateRequest;
import com.school_management_webapi.dto.response.PaymentResponse;
import com.school_management_webapi.entity.Payment;

public final class PaymentMapper {

	private PaymentMapper() {
	}

	public static Payment toEntity(PaymentCreateRequest request) {
		return Payment.builder()
				.branchId(request.branchId())
				.reference(request.reference())
				.feeId(request.feeId())
				.studentId(request.studentId())
				.amount(request.amount())
				.paidOn(request.paidOn())
				.method(request.method())
				.status(request.status())
				.payerName(request.payerName())
				.notes(request.notes())
				.build();
	}

	public static void updateEntity(Payment entity, PaymentUpdateRequest request) {
		entity.setBranchId(request.branchId());
		entity.setReference(request.reference());
		entity.setFeeId(request.feeId());
		entity.setStudentId(request.studentId());
		entity.setAmount(request.amount());
		entity.setPaidOn(request.paidOn());
		entity.setMethod(request.method());
		entity.setStatus(request.status());
		entity.setPayerName(request.payerName());
		entity.setNotes(request.notes());
	}

	public static void patchEntity(Payment entity, PaymentPatchRequest request) {
		if (request.branchId() != null) {
			entity.setBranchId(request.branchId());
		}

		if (request.reference() != null) {
			entity.setReference(request.reference());
		}

		if (request.feeId() != null) {
			entity.setFeeId(request.feeId());
		}

		if (request.studentId() != null) {
			entity.setStudentId(request.studentId());
		}

		if (request.amount() != null) {
			entity.setAmount(request.amount());
		}

		if (request.paidOn() != null) {
			entity.setPaidOn(request.paidOn());
		}

		if (request.method() != null) {
			entity.setMethod(request.method());
		}

		if (request.status() != null) {
			entity.setStatus(request.status());
		}

		if (request.payerName() != null) {
			entity.setPayerName(request.payerName());
		}

		if (request.notes() != null) {
			entity.setNotes(request.notes());
		}
	}

	public static PaymentResponse toResponse(Payment entity) {
		return new PaymentResponse(
				entity.getId(),
				entity.getBranchId(),
				entity.getReference(),
				entity.getFeeId(),
				entity.getStudentId(),
				entity.getAmount(),
				entity.getPaidOn(),
				entity.getMethod(),
				entity.getStatus(),
				entity.getPayerName(),
				entity.getNotes(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

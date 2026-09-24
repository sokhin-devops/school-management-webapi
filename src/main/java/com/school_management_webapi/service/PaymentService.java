package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.PaymentCreateRequest;
import com.school_management_webapi.dto.request.PaymentPatchRequest;
import com.school_management_webapi.dto.request.PaymentUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.PaymentResponse;
import com.school_management_webapi.entity.PaymentMethod;
import com.school_management_webapi.entity.PaymentStatus;

public interface PaymentService {

	PaymentResponse create(UUID userId, PaymentCreateRequest request);

	PaymentResponse getById(UUID userId, UUID id);

	PagedResponse<PaymentResponse> list(UUID userId, UUID branchId, PaymentStatus status, PaymentMethod method, UUID feeId, UUID studentId, String search, Pageable pageable);

	PaymentResponse update(UUID userId, UUID id, PaymentUpdateRequest request);

	PaymentResponse patch(UUID userId, UUID id, PaymentPatchRequest request);

	void delete(UUID userId, UUID id);
}

package com.school_management_webapi.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.school_management_webapi.dto.request.ExpenseCreateRequest;
import com.school_management_webapi.dto.request.ExpensePatchRequest;
import com.school_management_webapi.dto.request.ExpenseUpdateRequest;
import com.school_management_webapi.dto.response.ExpenseResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.ExpenseStatus;

public interface ExpenseService {

	ExpenseResponse create(UUID userId, ExpenseCreateRequest request);

	ExpenseResponse getById(UUID userId, UUID id);

	PagedResponse<ExpenseResponse> list(UUID userId, UUID branchId, ExpenseStatus status, String search, Pageable pageable);

	ExpenseResponse update(UUID userId, UUID id, ExpenseUpdateRequest request);

	ExpenseResponse patch(UUID userId, UUID id, ExpensePatchRequest request);

	void delete(UUID userId, UUID id);
}

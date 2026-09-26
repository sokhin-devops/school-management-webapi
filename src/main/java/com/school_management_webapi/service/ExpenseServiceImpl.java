package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.ExpenseCreateRequest;
import com.school_management_webapi.dto.request.ExpensePatchRequest;
import com.school_management_webapi.dto.request.ExpenseUpdateRequest;
import com.school_management_webapi.dto.response.ExpenseResponse;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.entity.Expense;
import com.school_management_webapi.entity.ExpenseStatus;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.ExpenseMapper;
import com.school_management_webapi.repository.ExpenseRepository;
import com.school_management_webapi.specification.ExpenseSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

	private final ExpenseRepository expenseRepository;
	private final BranchScopeService branchScopeService;
	private final NotificationService notificationService;

	@Override
	public ExpenseResponse create(UUID userId, ExpenseCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		Expense entity = ExpenseMapper.toEntity(request);
		ExpenseResponse created = ExpenseMapper.toResponse(expenseRepository.saveAndFlush(entity));
		// Only a pending expense is waiting on anyone; one entered as already
		// approved or paid needs nothing from the people told.
		if (request.status() == ExpenseStatus.PENDING) {
			notificationService.notifySchool(userId, NotificationEvent.EXPENSE_SUBMITTED,
					request.category() + ": " + request.amount().toPlainString() + " is waiting for approval.",
					"/finance/expenses");
		}
		return created;
	}

	@Override
	@Transactional(readOnly = true)
	public ExpenseResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return ExpenseMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<ExpenseResponse> list(UUID userId, UUID branchId, ExpenseStatus status, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Expense> page = expenseRepository.findAll(
				ExpenseSpecification.filterBy(allowedBranchIds, branchId, status, search), pageable);
		return PagedResponse.of(page.map(ExpenseMapper::toResponse));
	}

	@Override
	public ExpenseResponse update(UUID userId, UUID id, ExpenseUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Expense entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		ExpenseMapper.updateEntity(entity, request);
		return ExpenseMapper.toResponse(expenseRepository.saveAndFlush(entity));
	}

	@Override
	public ExpenseResponse patch(UUID userId, UUID id, ExpensePatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Expense entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}

		ExpenseMapper.patchEntity(entity, request);
		return ExpenseMapper.toResponse(expenseRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		expenseRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Expense findInTenantOrThrow(UUID id, UUID tenantId) {
		Expense entity = expenseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}
}

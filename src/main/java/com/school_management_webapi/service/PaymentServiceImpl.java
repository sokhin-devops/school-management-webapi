package com.school_management_webapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school_management_webapi.dto.request.PaymentCreateRequest;
import com.school_management_webapi.dto.request.PaymentPatchRequest;
import com.school_management_webapi.dto.request.PaymentUpdateRequest;
import com.school_management_webapi.dto.response.PagedResponse;
import com.school_management_webapi.dto.response.PaymentResponse;
import com.school_management_webapi.entity.Payment;
import com.school_management_webapi.entity.PaymentMethod;
import com.school_management_webapi.entity.PaymentStatus;
import com.school_management_webapi.exception.DuplicateResourceException;
import com.school_management_webapi.exception.ResourceNotFoundException;
import com.school_management_webapi.mapper.PaymentMapper;
import com.school_management_webapi.repository.PaymentRepository;
import com.school_management_webapi.specification.PaymentSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final BranchScopeService branchScopeService;

	@Override
	public PaymentResponse create(UUID userId, PaymentCreateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);

		if (paymentRepository.existsByReferenceAndBranchId(request.reference(), request.branchId())) {
			throw new DuplicateResourceException(
					"A payment with reference '" + request.reference() + "' already exists in this branch");
		}

		Payment entity = PaymentMapper.toEntity(request);
		return PaymentMapper.toResponse(paymentRepository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentResponse getById(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		return PaymentMapper.toResponse(findInTenantOrThrow(id, tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<PaymentResponse> list(UUID userId, UUID branchId, PaymentStatus status, PaymentMethod method, UUID feeId, UUID studentId, String search, Pageable pageable) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		if (branchId != null) {
			branchScopeService.requireBranchInTenant(branchId, tenantId);
		}

		List<UUID> allowedBranchIds = branchScopeService.allowedBranchIds(tenantId);
		Page<Payment> page = paymentRepository.findAll(
				PaymentSpecification.filterBy(allowedBranchIds, branchId, status, method, feeId, studentId, search), pageable);
		return PagedResponse.of(page.map(PaymentMapper::toResponse));
	}

	@Override
	public PaymentResponse update(UUID userId, UUID id, PaymentUpdateRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Payment entity = findInTenantOrThrow(id, tenantId);
		branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		ensureReferenceIsAvailable(id, request.reference(), request.branchId());

		PaymentMapper.updateEntity(entity, request);
		return PaymentMapper.toResponse(paymentRepository.saveAndFlush(entity));
	}

	@Override
	public PaymentResponse patch(UUID userId, UUID id, PaymentPatchRequest request) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		Payment entity = findInTenantOrThrow(id, tenantId);
		if (request.branchId() != null) {
			branchScopeService.requireBranchInTenant(request.branchId(), tenantId);
		}
		ensureReferenceIsAvailable(id, request.reference() != null ? request.reference() : entity.getReference(),
				request.branchId() != null ? request.branchId() : entity.getBranchId());

		PaymentMapper.patchEntity(entity, request);
		return PaymentMapper.toResponse(paymentRepository.saveAndFlush(entity));
	}

	@Override
	public void delete(UUID userId, UUID id) {
		UUID tenantId = branchScopeService.requireTenantId(userId);
		paymentRepository.delete(findInTenantOrThrow(id, tenantId));
	}

	/**
	 * A record outside the caller's tenant is reported as not found rather than
	 * forbidden, so ids cannot be probed across tenants.
	 */
	private Payment findInTenantOrThrow(UUID id, UUID tenantId) {
		Payment entity = paymentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

		branchScopeService.requireBranchInTenant(entity.getBranchId(), tenantId);
		return entity;
	}

	private void ensureReferenceIsAvailable(UUID id, String reference, UUID branchId) {
		paymentRepository.findByReferenceAndBranchId(reference, branchId)
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new DuplicateResourceException(
							"A payment with reference '" + reference + "' already exists in this branch");
				});
	}
}

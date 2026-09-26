package com.school_management_webapi.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * What the platform bills a school for one subscription period - 68-subscription.md.
 *
 * Raised when a plan is chosen or changed. The plan's name and price are copied
 * onto the invoice, so a later price change never rewrites what was billed.
 * No payment processor is connected, so an invoice is issued, not collected.
 */
@Entity
@Table(name = "subscription_invoices",
		uniqueConstraints = @UniqueConstraint(name = "uk_subscription_invoices_tenant_number",
				columnNames = { "tenant_id", "number" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionInvoice {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "subscription_id", nullable = false)
	private UUID subscriptionId;

	/** Numbered per school: INV-2026-0001. */
	@Column(name = "number", nullable = false, length = 40)
	private String number;

	@Column(name = "plan_name", nullable = false, length = 100)
	private String planName;

	@Enumerated(EnumType.STRING)
	@Column(name = "billing_cycle", nullable = false, length = 20)
	private BillingCycle billingCycle;

	@Column(name = "amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Column(name = "period_start", nullable = false)
	private LocalDateTime periodStart;

	@Column(name = "period_end")
	private LocalDateTime periodEnd;

	/** Who it is addressed to, copied at the time like the plan. */
	@Column(name = "bill_to_name", length = 255)
	private String billToName;

	@Column(name = "bill_to_email", length = 255)
	private String billToEmail;

	@Column(name = "bill_to_address", length = 1000)
	private String billToAddress;

	@CreationTimestamp
	@Column(name = "issued_at", nullable = false, updatable = false)
	private LocalDateTime issuedAt;
}

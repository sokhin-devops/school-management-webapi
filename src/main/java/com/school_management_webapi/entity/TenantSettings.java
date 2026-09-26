package com.school_management_webapi.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row per tenant holding everything Settings configures that is not a
 * record of its own - school profile and branches are, and live elsewhere.
 *
 * Created with defaults the first time anything reads it, so no tenant ever
 * has to have been "set up" for its settings to exist.
 */
@Entity
@Table(name = "tenant_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantSettings {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "tenant_id", nullable = false, unique = true)
	private UUID tenantId;

	@Embedded
	@Builder.Default
	private SecurityPolicy security = SecurityPolicy.defaults();

	@Embedded
	@Builder.Default
	private AcademicPreferences academic = AcademicPreferences.defaults();

	/** Non-owners are refused while this is on - 69-system-settings.md. */
	@Column(name = "maintenance_mode", nullable = false)
	private boolean maintenanceMode;

	@Column(name = "billing_email", length = 255)
	private String billingEmail;

	@Column(name = "billing_address", length = 1000)
	private String billingAddress;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}

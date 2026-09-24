package com.school_management_webapi.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tenant_users", uniqueConstraints = @UniqueConstraint(columnNames = { "tenant_id", "user_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantUser {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/**
	 * The coarse membership level auth checks against. Kept alongside roleId
	 * because the owner of a tenant must stay recognisable even while the
	 * permission grid below is being edited.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TenantUserRole role;

	/** The role from Settings that carries this user's permission grid. */
	@Column(name = "role_id")
	private UUID roleId;

	/** Empty means every branch of the tenant. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "tenant_user_branches", joinColumns = @JoinColumn(name = "tenant_user_id"))
	@Column(name = "branch_id", nullable = false)
	@Builder.Default
	private List<UUID> branchIds = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}

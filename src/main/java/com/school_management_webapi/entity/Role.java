package com.school_management_webapi.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * What a user is allowed to do, and where.
 *
 * Scoped to the tenant rather than to a branch, because the same role is what a
 * person holds across the branches they work in; which branches those are is
 * the list below rather than a separate role per branch.
 */
@Entity
@Table(name = "roles",
		uniqueConstraints = @UniqueConstraint(name = "uk_roles_tenant_id_name", columnNames = { "tenant_id", "name" }))
@SQLDelete(sql = "UPDATE roles SET deleted_at = now(), updated_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "name", nullable = false, length = 80)
	private String name;

	/** A seeded role. Cannot be edited or deleted - 64-users-and-roles.md. */
	@Column(name = "is_default", nullable = false)
	private boolean defaultRole;

	@Enumerated(EnumType.STRING)
	@Column(name = "default_type", length = 20)
	private DefaultRoleType defaultType;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
	@Builder.Default
	private Set<RolePermission> permissions = new LinkedHashSet<>();

	/** Empty means every branch of the tenant, which is what the default roles hold. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "role_branches", joinColumns = @JoinColumn(name = "role_id"))
	@Column(name = "branch_id", nullable = false)
	@Builder.Default
	private List<UUID> branchIds = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}

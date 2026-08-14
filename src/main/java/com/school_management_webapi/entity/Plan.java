package com.school_management_webapi.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(nullable = false, unique = true, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "price_monthly", nullable = false, precision = 10, scale = 2)
	private BigDecimal priceMonthly;

	@Column(name = "price_yearly", nullable = false, precision = 10, scale = 2)
	private BigDecimal priceYearly;

	@Column(nullable = false, length = 10)
	private String currency;

	@Column(name = "max_students")
	private Integer maxStudents;

	@Column(name = "max_teachers")
	private Integer maxTeachers;

	@Column(name = "max_branches")
	private Integer maxBranches;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PlanStatus status;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
	@Builder.Default
	private List<PlanFeature> features = new ArrayList<>();
}

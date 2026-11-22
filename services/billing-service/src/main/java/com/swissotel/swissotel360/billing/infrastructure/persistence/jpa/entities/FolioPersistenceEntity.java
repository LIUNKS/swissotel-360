package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "folios")
@Getter
@Setter
public class FolioPersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "public_id", nullable = false, unique = true, updatable = false)
	private UUID publicId;
	@Column(name = "stay_id", nullable = false, unique = true, updatable = false)
	private Long stayId;
	@Column(nullable = false, length = 20)
	private String status;
	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;
}
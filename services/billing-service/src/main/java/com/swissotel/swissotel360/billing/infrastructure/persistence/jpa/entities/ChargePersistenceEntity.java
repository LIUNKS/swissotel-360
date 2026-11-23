package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "charges")
@Getter
@Setter
public class ChargePersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "public_id", nullable = false, unique = true, updatable = false)
	private UUID publicId;
	@Column(name = "folio_id", nullable = false, updatable = false)
	private Long folioId;
	@Column(name = "service_request_id", updatable = false)
	private Long serviceRequestId;
	@Column(nullable = false, length = 120, updatable = false)
	private String concept;
	@Column(nullable = false, precision = 10, scale = 2, updatable = false)
	private BigDecimal amount;
	@Column(name = "charged_at", nullable = false, updatable = false)
	private Instant chargedAt;
}
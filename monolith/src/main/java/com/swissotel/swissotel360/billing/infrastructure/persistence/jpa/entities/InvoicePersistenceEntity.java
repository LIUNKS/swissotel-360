package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
public class InvoicePersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "public_id", nullable = false, unique = true, updatable = false)
	private UUID publicId;
	@Column(name = "payment_id", nullable = false, unique = true, updatable = false)
	private Long paymentId;
	@Column(name = "document_type", nullable = false, length = 10, updatable = false)
	private String documentType;
	@Column(nullable = false, length = 10, updatable = false)
	private String series;
	@Column(nullable = false, length = 20, updatable = false)
	private String number;
	@Column(name = "issued_at", nullable = false, updatable = false)
	private Instant issuedAt;
	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2, updatable = false)
	private BigDecimal totalAmount;
}
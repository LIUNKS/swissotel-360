package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class PaymentPersistenceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "public_id", nullable = false, unique = true, updatable = false)
	private UUID publicId;
	@Column(name = "folio_id", nullable = false, unique = true, updatable = false)
	private Long folioId;
	@Column(name = "payment_method_id", nullable = false, updatable = false)
	private Long paymentMethodId;
	@Column(nullable = false, precision = 10, scale = 2, updatable = false)
	private BigDecimal amount;
	@Column(nullable = false, length = 20)
	private String status;
	@Column(name = "gateway_reference", length = 100)
	private String gatewayReference;
	@Column(name = "paid_at", nullable = false, updatable = false)
	private Instant paidAt;
}
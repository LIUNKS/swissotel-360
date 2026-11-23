package com.swissotel.swissotel360.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Folio line item; dbId and serviceRequestId are internal database ids, publicId is the external identifier. */
public record Charge(Long dbId, UUID publicId, Long serviceRequestId, String concept, BigDecimal amount,
		Instant chargedAt) {
	public Charge {
		if (publicId == null || concept == null || concept.isBlank() || chargedAt == null)
			throw new IllegalArgumentException("Charge fields are required");
		concept = concept.trim();
		if (concept.length() > 120)
			throw new IllegalArgumentException("concept must be at most 120 characters");
		amount = Money.of(amount);
	}
}

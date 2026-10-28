package com.swissotel.swissotel360.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/** Folio line item; dbId and serviceRequestId are internal ids (charges have no public id). */
public record Charge(Long dbId, Long serviceRequestId, String concept, BigDecimal amount, Instant chargedAt) {
	public Charge {
		if (concept == null || concept.isBlank() || chargedAt == null)
			throw new IllegalArgumentException("Charge fields are required");
		concept = concept.trim();
		if (concept.length() > 120)
			throw new IllegalArgumentException("concept must be at most 120 characters");
		amount = Money.of(amount);
	}
}

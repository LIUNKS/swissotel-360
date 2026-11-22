package com.swissotel.swissotel360.billing.domain.model;

import java.util.UUID;

public record PaymentId(UUID value) {
	public PaymentId {
		if (value == null)
			throw new IllegalArgumentException("payment id is required");
	}

	public static PaymentId newId() {
		return new PaymentId(UUID.randomUUID());
	}
}

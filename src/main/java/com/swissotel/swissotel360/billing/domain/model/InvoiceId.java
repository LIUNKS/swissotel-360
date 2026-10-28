package com.swissotel.swissotel360.billing.domain.model;

import java.util.UUID;

public record InvoiceId(UUID value) {
	public InvoiceId {
		if (value == null)
			throw new IllegalArgumentException("invoice id is required");
	}

	public static InvoiceId newId() {
		return new InvoiceId(UUID.randomUUID());
	}
}

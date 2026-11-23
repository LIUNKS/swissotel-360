package com.swissotel.swissotel360.billing.domain.model;

import java.util.UUID;

public record FolioId(UUID value) {
	public FolioId {
		if (value == null)
			throw new IllegalArgumentException("folio id is required");
	}

	public static FolioId newId() {
		return new FolioId(UUID.randomUUID());
	}
}

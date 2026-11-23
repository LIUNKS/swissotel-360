package com.swissotel.swissotel360.stays.domain.model;

import java.util.UUID;

public record StayId(UUID value) {
	public StayId {
		if (value == null)
			throw new IllegalArgumentException("stay id is required");
	}

	public static StayId newId() {
		return new StayId(UUID.randomUUID());
	}
}

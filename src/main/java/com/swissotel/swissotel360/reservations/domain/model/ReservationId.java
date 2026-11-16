package com.swissotel.swissotel360.reservations.domain.model;

import java.util.UUID;

public record ReservationId(UUID value) {
	public ReservationId {
		if (value == null)
			throw new IllegalArgumentException("reservation id is required");
	}

	public static ReservationId newId() {
		return new ReservationId(UUID.randomUUID());
	}
}

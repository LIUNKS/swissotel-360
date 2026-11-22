package com.swissotel.swissotel360.reservations.domain.model;

public record GuestId(Long value) {
	public GuestId {
		if (value == null || value < 1)
			throw new IllegalArgumentException("guest id must be positive");
	}
}

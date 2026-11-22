package com.swissotel.swissotel360.reservations.domain.model;

public record UserId(Long value) {
	public UserId {
		if (value == null || value < 1)
			throw new IllegalArgumentException("user id must be positive");
	}
}

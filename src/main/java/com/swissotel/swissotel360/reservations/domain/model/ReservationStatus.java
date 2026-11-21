package com.swissotel.swissotel360.reservations.domain.model;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public enum ReservationStatus {
	PENDING, CONFIRMED, CANCELLED, COMPLETED;

	private static final Map<ReservationStatus, Set<ReservationStatus>> TRANSITIONS = Map.of(
			PENDING, Set.of(CONFIRMED, CANCELLED),
			CONFIRMED, Set.of(CANCELLED, COMPLETED),
			CANCELLED, Set.of(), COMPLETED, Set.of());

	public boolean canTransitionTo(ReservationStatus target) {
		return TRANSITIONS.get(this).contains(target);
	}

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static ReservationStatus fromCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Invalid reservation status: " + code);
		}
		var normalized = code.trim().toUpperCase(Locale.ROOT);
		for (var status : values()) {
			if (status.name().equals(normalized)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Invalid reservation status: " + code);
	}
}

package com.swissotel.swissotel360.reservations.domain.model;

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
		return name().toLowerCase(java.util.Locale.ROOT);
	}

	public static ReservationStatus fromCode(String code) {
		return valueOf(code.toUpperCase(java.util.Locale.ROOT));
	}
}

package com.swissotel.swissotel360.reservations.domain.model;

import java.util.Locale;

public enum CancellationReason {
	GUEST_REQUEST, STAFF, PAYMENT_TIMEOUT;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static CancellationReason fromCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Invalid cancellation reason: " + code);
		}
		var normalized = code.trim().toUpperCase(Locale.ROOT);
		for (var reason : values()) {
			if (reason.name().equals(normalized)) {
				return reason;
			}
		}
		throw new IllegalArgumentException("Invalid cancellation reason: " + code);
	}
}

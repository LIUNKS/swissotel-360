package com.swissotel.swissotel360.billing.domain.model;

import java.util.Locale;

public enum PaymentStatus {
	PENDING, PROCESSED, REJECTED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static PaymentStatus fromCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Invalid payment status: " + code);
		}
		var normalized = code.trim().toUpperCase(Locale.ROOT);
		for (var value : values()) {
			if (value.name().equals(normalized)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid payment status: " + code);
	}
}

package com.swissotel.swissotel360.reservations.domain.model;

import java.util.Locale;

public enum PaymentType {
	IMMEDIATE, DEFERRED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static PaymentType fromCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Invalid payment type: " + code);
		}
		var normalized = code.trim().toUpperCase(Locale.ROOT);
		for (var type : values()) {
			if (type.name().equals(normalized)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Invalid payment type: " + code);
	}
}

package com.swissotel.swissotel360.stays.domain.model;

import java.util.Locale;

public enum StayStatus {
	ACTIVE, CLOSED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static StayStatus fromCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Invalid stay status: " + code);
		}
		var normalized = code.trim().toUpperCase(Locale.ROOT);
		for (var value : values()) {
			if (value.name().equals(normalized)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid stay status: " + code);
	}
}

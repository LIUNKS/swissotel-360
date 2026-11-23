package com.swissotel.swissotel360.stays.domain.model;

import java.util.Locale;

public enum KeyStatus {
	ACTIVE, REVOKED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static KeyStatus fromCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Invalid key status: " + code);
		}
		var normalized = code.trim().toUpperCase(Locale.ROOT);
		for (var value : values()) {
			if (value.name().equals(normalized)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid key status: " + code);
	}
}

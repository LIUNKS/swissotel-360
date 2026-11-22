package com.swissotel.swissotel360.billing.domain.model;

import java.util.Locale;

public enum FolioStatus {
	OPEN, CLOSED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static FolioStatus fromCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Invalid folio status: " + code);
		}
		var normalized = code.trim().toUpperCase(Locale.ROOT);
		for (var value : values()) {
			if (value.name().equals(normalized)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid folio status: " + code);
	}
}

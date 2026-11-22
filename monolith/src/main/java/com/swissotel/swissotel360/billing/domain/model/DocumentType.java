package com.swissotel.swissotel360.billing.domain.model;

import java.util.Locale;

public enum DocumentType {
	RECEIPT("B001"), INVOICE("F001");

	private final String series;

	DocumentType(String series) {
		this.series = series;
	}

	public String series() {
		return series;
	}

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static DocumentType fromCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Invalid document type: " + code);
		}
		var normalized = code.trim().toUpperCase(Locale.ROOT);
		for (var value : values()) {
			if (value.name().equals(normalized)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid document type: " + code);
	}
}

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
		return valueOf(code.toUpperCase(Locale.ROOT));
	}
}

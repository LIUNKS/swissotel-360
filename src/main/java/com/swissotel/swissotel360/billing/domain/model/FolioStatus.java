package com.swissotel.swissotel360.billing.domain.model;

import java.util.Locale;

public enum FolioStatus {
	OPEN, CLOSED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static FolioStatus fromCode(String code) {
		return valueOf(code.toUpperCase(Locale.ROOT));
	}
}

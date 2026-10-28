package com.swissotel.swissotel360.stays.domain.model;

import java.util.Locale;

public enum StayStatus {
	ACTIVE, CLOSED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static StayStatus fromCode(String code) {
		return valueOf(code.toUpperCase(Locale.ROOT));
	}
}

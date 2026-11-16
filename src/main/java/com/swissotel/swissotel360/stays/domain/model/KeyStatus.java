package com.swissotel.swissotel360.stays.domain.model;

import java.util.Locale;

public enum KeyStatus {
	ACTIVE, REVOKED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static KeyStatus fromCode(String code) {
		return valueOf(code.toUpperCase(Locale.ROOT));
	}
}

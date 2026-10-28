package com.swissotel.swissotel360.billing.domain.model;

import java.util.Locale;

public enum PaymentStatus {
	PENDING, PROCESSED, REJECTED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static PaymentStatus fromCode(String code) {
		return valueOf(code.toUpperCase(Locale.ROOT));
	}
}

package com.swissotel.swissotel360.servicerequests.domain.model;

import java.util.Locale;

public enum ServiceRequestStatus {
	REGISTERED, IN_PROGRESS, COMPLETED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static ServiceRequestStatus fromCode(String code) {
		return valueOf(code.toUpperCase(Locale.ROOT));
	}
}

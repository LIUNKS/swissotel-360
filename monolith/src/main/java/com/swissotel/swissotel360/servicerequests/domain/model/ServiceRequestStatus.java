package com.swissotel.swissotel360.servicerequests.domain.model;

import java.util.Locale;

public enum ServiceRequestStatus {
	REGISTERED, IN_PROGRESS, COMPLETED;

	public String code() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static ServiceRequestStatus fromCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Invalid service request status: " + code);
		}
		var normalized = code.trim().toUpperCase(Locale.ROOT);
		for (var value : values()) {
			if (value.name().equals(normalized)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid service request status: " + code);
	}
}

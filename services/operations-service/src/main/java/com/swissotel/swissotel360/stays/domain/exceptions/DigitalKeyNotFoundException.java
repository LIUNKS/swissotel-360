package com.swissotel.swissotel360.stays.domain.exceptions;

import java.util.UUID;

public class DigitalKeyNotFoundException extends RuntimeException {
	public DigitalKeyNotFoundException(UUID keyPublicId) {
		super("Digital key not found: " + keyPublicId);
	}
}

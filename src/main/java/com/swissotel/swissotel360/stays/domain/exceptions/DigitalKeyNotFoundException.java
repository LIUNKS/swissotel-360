package com.swissotel.swissotel360.stays.domain.exceptions;

public class DigitalKeyNotFoundException extends RuntimeException {
	public DigitalKeyNotFoundException(Long id) {
		super("Digital key not found: " + id);
	}
}

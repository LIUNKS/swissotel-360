package com.swissotel.swissotel360.stays.domain.exceptions;

import java.util.UUID;

public class StayNotFoundException extends RuntimeException {
	public StayNotFoundException(UUID id) {
		super("Stay not found: " + id);
	}
}

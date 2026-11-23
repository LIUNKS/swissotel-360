package com.swissotel.swissotel360.rooms.domain.exceptions;

import java.util.UUID;

public class RateEntityNotFoundException extends RuntimeException {
	public RateEntityNotFoundException(UUID publicId) {
		super("Rate not found: " + publicId);
	}
}

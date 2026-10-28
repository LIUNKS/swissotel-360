package com.swissotel.swissotel360.rooms.domain.exceptions;

public class RateEntityNotFoundException extends RuntimeException {
	public RateEntityNotFoundException(Long id) {
		super("Rate not found: " + id);
	}
}

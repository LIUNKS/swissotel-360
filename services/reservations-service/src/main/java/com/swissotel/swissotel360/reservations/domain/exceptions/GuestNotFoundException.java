package com.swissotel.swissotel360.reservations.domain.exceptions;

import java.util.UUID;

public class GuestNotFoundException extends RuntimeException {
	public GuestNotFoundException(UUID id) {
		super("Guest not found: " + id);
	}

	public GuestNotFoundException(Long id) {
		super("Guest not found: " + id);
	}
}

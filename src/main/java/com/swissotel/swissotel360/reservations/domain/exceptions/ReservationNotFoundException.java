package com.swissotel.swissotel360.reservations.domain.exceptions;

import java.util.UUID;

public class ReservationNotFoundException extends RuntimeException {
	public ReservationNotFoundException(UUID id) {
		super("Reservation not found: " + id);
	}

	public ReservationNotFoundException(String code) {
		super("Reservation not found: " + code);
	}
}

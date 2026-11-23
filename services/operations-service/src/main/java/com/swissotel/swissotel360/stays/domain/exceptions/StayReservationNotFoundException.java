package com.swissotel.swissotel360.stays.domain.exceptions;

public class StayReservationNotFoundException extends RuntimeException {
	public StayReservationNotFoundException(String code) {
		super("Reservation not found: " + code);
	}
}

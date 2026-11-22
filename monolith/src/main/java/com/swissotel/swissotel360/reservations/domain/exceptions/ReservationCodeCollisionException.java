package com.swissotel.swissotel360.reservations.domain.exceptions;

public class ReservationCodeCollisionException extends RuntimeException {
	public ReservationCodeCollisionException() {
		super("Reservation code is already in use");
	}
}

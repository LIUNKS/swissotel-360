package com.swissotel.swissotel360.stays.domain.exceptions;

public class ReservationNotEligibleForCheckInException extends RuntimeException {
	public ReservationNotEligibleForCheckInException(String reason) {
		super("Reservation not eligible for check-in: " + reason);
	}
}

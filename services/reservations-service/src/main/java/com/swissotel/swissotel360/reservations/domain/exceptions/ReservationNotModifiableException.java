package com.swissotel.swissotel360.reservations.domain.exceptions;

import com.swissotel.swissotel360.reservations.domain.model.ReservationId;
import com.swissotel.swissotel360.reservations.domain.model.ReservationStatus;

public class ReservationNotModifiableException extends RuntimeException {
	public ReservationNotModifiableException(ReservationId id, ReservationStatus status) {
		super("Reservation " + id.value() + " is not modifiable while " + status);
	}
}

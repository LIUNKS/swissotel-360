package com.swissotel.swissotel360.reservations.domain.exceptions;

import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;

public class NoAvailabilityException extends RuntimeException {
	public NoAvailabilityException(RoomTypeId id) {
		super("No availability for room type " + id.value());
	}
}

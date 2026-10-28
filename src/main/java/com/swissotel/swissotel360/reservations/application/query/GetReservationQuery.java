package com.swissotel.swissotel360.reservations.application.query;

import java.util.UUID;

public record GetReservationQuery(UUID reservationPublicId) {
	public GetReservationQuery {
		if (reservationPublicId == null)
			throw new IllegalArgumentException("Reservation id is required");
	}
}

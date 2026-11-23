package com.swissotel.swissotel360.reservations.application.command;

import java.util.UUID;

public record CompleteReservationCommand(UUID reservationPublicId) {
	public CompleteReservationCommand {
		if (reservationPublicId == null)
			throw new IllegalArgumentException("Reservation id is required");
	}
}

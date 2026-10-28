package com.swissotel.swissotel360.reservations.application.command;

import java.util.UUID;

public record ConfirmReservationCommand(UUID reservationPublicId) {
	public ConfirmReservationCommand {
		if (reservationPublicId == null)
			throw new IllegalArgumentException("Reservation id is required");
	}
}

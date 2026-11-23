package com.swissotel.swissotel360.reservations.application.command;

import java.util.UUID;

public record CancelReservationCommand(UUID reservationPublicId, String note) {
	public CancelReservationCommand {
		if (reservationPublicId == null)
			throw new IllegalArgumentException("Reservation id is required");
		if (note != null && note.length() > 200)
			throw new IllegalArgumentException("Cancellation note cannot exceed 200 characters");
	}
}

package com.swissotel.swissotel360.reservations.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record ModifyReservationCommand(UUID reservationPublicId, UUID roomTypePublicId, LocalDate checkInDate,
		LocalDate checkOutDate) {
	public ModifyReservationCommand {
		if (reservationPublicId == null || roomTypePublicId == null || checkInDate == null || checkOutDate == null)
			throw new IllegalArgumentException("Reservation, room type and stay dates are required");
	}
}

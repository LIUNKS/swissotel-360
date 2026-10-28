package com.swissotel.swissotel360.reservations.application.command;

import com.swissotel.swissotel360.reservations.domain.model.PaymentType;
import java.time.LocalDate;
import java.util.UUID;

public record CreateReservationCommand(UUID guestPublicId, UUID roomTypePublicId, LocalDate checkInDate,
		LocalDate checkOutDate, PaymentType paymentType) {
	public CreateReservationCommand {
		if (roomTypePublicId == null || checkInDate == null || checkOutDate == null || paymentType == null)
			throw new IllegalArgumentException("Room type, stay dates and payment type are required");
	}
}

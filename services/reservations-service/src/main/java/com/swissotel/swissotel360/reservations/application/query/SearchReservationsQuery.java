package com.swissotel.swissotel360.reservations.application.query;

import com.swissotel.swissotel360.reservations.domain.model.ReservationStatus;
import java.time.LocalDate;
import java.util.UUID;

public record SearchReservationsQuery(ReservationStatus status, UUID guestPublicId, String code,
		LocalDate checkInFrom, LocalDate checkInTo, int page, int size) {
}

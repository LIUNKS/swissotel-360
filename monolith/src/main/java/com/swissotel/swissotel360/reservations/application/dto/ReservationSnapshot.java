package com.swissotel.swissotel360.reservations.application.dto;

import com.swissotel.swissotel360.reservations.domain.model.GuestId;
import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;
import java.time.LocalDate;
import java.util.UUID;

public record ReservationSnapshot(UUID publicId, String code, GuestId guestId, RoomTypeId roomTypeId,
		LocalDate checkIn, LocalDate checkOut, String status, Long internalId) {
}

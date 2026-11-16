package com.swissotel.swissotel360.reservations.application.dto;

import com.swissotel.swissotel360.reservations.application.port.GuestDirectoryPort.GuestSnapshot;
import com.swissotel.swissotel360.reservations.application.port.PrepaymentPort;
import com.swissotel.swissotel360.reservations.application.port.RoomInventoryPort.PriceQuote;
import com.swissotel.swissotel360.reservations.application.port.RoomInventoryPort.RoomTypeSnapshot;
import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import java.time.Instant;

public record ReservationResult(Reservation reservation, GuestSnapshot guest, RoomTypeSnapshot roomType,
		PriceQuote quote, Instant holdExpiresAt, PrepaymentPort.PrepaymentRequest prepayment) {
}

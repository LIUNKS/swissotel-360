package com.swissotel.swissotel360.reservations.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Reservation row for search results")
public record ReservationSummaryResource(UUID publicId, String reservationCode, String status, UUID guestPublicId,
		String guestName,
		UUID roomTypePublicId, String roomTypeName, LocalDate checkInDate, LocalDate checkOutDate, String paymentType,
		BigDecimal totalAmount, Instant createdAt) {
}

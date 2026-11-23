package com.swissotel.swissotel360.reservations.infrastructure.http;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RoomQuoteCollaboratorSnapshot(
		UUID roomTypePublicId,
		LocalDate checkIn,
		LocalDate checkOut,
		int nights,
		BigDecimal totalAmount,
		List<NightPrice> breakdown) {

	public record NightPrice(LocalDate date, BigDecimal price) {
	}
}

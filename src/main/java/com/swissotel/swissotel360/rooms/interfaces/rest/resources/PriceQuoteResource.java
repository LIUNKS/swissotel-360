package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Night-by-night price quote for one room type")
public record PriceQuoteResource(UUID roomTypePublicId, LocalDate checkIn, LocalDate checkOut, int nights,
		BigDecimal totalAmount, List<NightPriceResource> breakdown) {
	public PriceQuoteResource {
		breakdown = List.copyOf(breakdown);
	}
}

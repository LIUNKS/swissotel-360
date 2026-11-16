package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Availability and total quote for one room type")
public record AvailabilityResource(UUID roomTypePublicId, String roomTypeName, Short capacity, int availableRooms,
		BigDecimal pricePerNight, BigDecimal totalAmount) {
}

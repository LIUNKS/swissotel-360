package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Room type and its current nightly rate")
public record RoomTypeResource(UUID publicId, String name, Short capacity, String description,
		BigDecimal currentPrice) {
}

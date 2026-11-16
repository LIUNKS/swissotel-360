package com.swissotel.swissotel360.rooms.application.queryservices;

import java.math.BigDecimal;
import java.util.UUID;

public record AvailabilityResult(UUID roomTypePublicId, String roomTypeName, Short capacity, int availableRooms,
		BigDecimal pricePerNight, BigDecimal totalAmount) {
}

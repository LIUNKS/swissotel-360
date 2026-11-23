package com.swissotel.swissotel360.stays.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Stay")
public record StayResource(UUID stayId, UUID roomId, @Schema(example = "301") String roomNumber,
		@Schema(example = "active") String status, Instant checkedInAt, Instant checkedOutAt) {
}

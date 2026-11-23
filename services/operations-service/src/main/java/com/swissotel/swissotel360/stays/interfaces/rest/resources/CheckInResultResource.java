package com.swissotel.swissotel360.stays.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of a check-in, including the auto-issued digital key")
public record CheckInResultResource(UUID stayId, @Schema(example = "301") String roomNumber,
		@Schema(example = "K7M2Q9XA4TBD") String accessCode, @Schema(example = "active") String status,
		Instant checkedInAt) {
}

package com.swissotel.swissotel360.servicerequests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A service request")
public record ServiceRequestResource(UUID requestId, UUID stayId,
		@Schema(example = "room_service") String serviceTypeCode, @Schema(example = "Room Service") String serviceTypeName,
		String description, @Schema(example = "registered") String status, Instant requestedAt,
		Instant completedAt) {
}

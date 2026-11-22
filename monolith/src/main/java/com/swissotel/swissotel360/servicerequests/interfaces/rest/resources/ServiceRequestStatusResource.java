package com.swissotel.swissotel360.servicerequests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of a status change")
public record ServiceRequestStatusResource(UUID requestId, @Schema(example = "completed") String status,
		Instant completedAt) {
}

package com.swissotel.swissotel360.servicerequests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of registering a service request")
public record CreateServiceRequestResultResource(UUID requestId, @Schema(example = "registered") String status,
		Instant requestedAt) {
}

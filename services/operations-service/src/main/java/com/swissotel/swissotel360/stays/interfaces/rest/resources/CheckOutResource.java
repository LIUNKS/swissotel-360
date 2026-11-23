package com.swissotel.swissotel360.stays.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of a check-out")
public record CheckOutResource(UUID stayId, @Schema(example = "closed") String status, Instant checkedOutAt) {
}

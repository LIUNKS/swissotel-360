package com.swissotel.swissotel360.servicerequests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Registers a service request for an active stay")
public record CreateServiceRequestResource(@NotNull UUID stayId,
		@NotBlank @Size(max = 30) @Schema(example = "room_service") String serviceTypeCode,
		@Size(max = 1000) @Schema(example = "Two coffees to room 301") String description) {
}

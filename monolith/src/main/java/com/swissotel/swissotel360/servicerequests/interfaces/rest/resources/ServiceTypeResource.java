package com.swissotel.swissotel360.servicerequests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A requestable hotel service")
public record ServiceTypeResource(@Schema(example = "room_service") String code,
		@Schema(example = "Room Service") String name, @Schema(example = "F&B") String operationalArea) {
}

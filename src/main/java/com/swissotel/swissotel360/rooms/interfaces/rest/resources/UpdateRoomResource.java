package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Room update payload. The room number cannot be changed.")
public record UpdateRoomResource(
		@Schema(example = "d2719f15-76f4-4d3d-b743-1fa683e72cab") @NotNull UUID roomTypePublicId,
		@Schema(example = "6") @NotNull Short floor) {
}

package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Room creation payload")
public record CreateRoomResource(
		@Schema(example = "d2719f15-76f4-4d3d-b743-1fa683e72cab") @NotNull UUID roomTypePublicId,
		@Schema(example = "601") @NotBlank @Size(max = 10) String roomNumber,
		@Schema(example = "6") @NotNull Short floor) {
}

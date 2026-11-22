package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Room type creation payload")
public record CreateRoomTypeResource(@Schema(example = "Deluxe") @NotBlank @Size(max = 60) String name,
		@Schema(example = "3") @NotNull @Positive Short capacity,
		@Schema(example = "Room with a city view") String description) {
}

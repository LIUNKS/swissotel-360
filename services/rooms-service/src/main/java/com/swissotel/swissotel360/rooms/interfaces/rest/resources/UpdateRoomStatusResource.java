package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Room status update payload")
public record UpdateRoomStatusResource(@Schema(example = "CLEANING") @NotNull RoomStatus status) {
}

package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Physical room inventory item")
public record RoomResource(UUID publicId, String roomNumber, Short floor, UUID roomTypePublicId, String roomTypeName,
		RoomStatus status) {
}

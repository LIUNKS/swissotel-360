package com.swissotel.swissotel360.rooms.application.queryservices;

import com.swissotel.swissotel360.rooms.domain.model.aggregates.Room;
import java.util.UUID;

public record RoomView(Room room, UUID roomTypePublicId, String roomTypeName) {
}

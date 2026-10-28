package com.swissotel.swissotel360.rooms.interfaces.acl;

import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import java.util.UUID;

public record RoomStatusSnapshot(UUID publicId, RoomStatus status) {
}

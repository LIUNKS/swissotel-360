package com.swissotel.swissotel360.rooms.interfaces.acl;

import java.util.UUID;

public record RoomStatusSnapshot(UUID publicId, String status) {
}

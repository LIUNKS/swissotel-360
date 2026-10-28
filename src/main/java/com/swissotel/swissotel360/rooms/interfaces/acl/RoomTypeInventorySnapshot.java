package com.swissotel.swissotel360.rooms.interfaces.acl;

import java.util.UUID;
public record RoomTypeInventorySnapshot(Long id, UUID publicId, String name, short capacity) { }

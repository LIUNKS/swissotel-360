package com.swissotel.swissotel360.rooms.interfaces.acl;

import java.util.UUID;

public record RoomInfoSnapshot(Long id, UUID publicId, Long roomTypeId, String roomNumber, String status) {
	public boolean isAvailable() {
		return "AVAILABLE".equals(status);
	}
}

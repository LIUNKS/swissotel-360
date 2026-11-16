package com.swissotel.swissotel360.rooms.domain.exceptions;

import java.util.UUID;

public class RoomNotFoundException extends RuntimeException {
	public RoomNotFoundException(UUID id) {
		super("Room not found: " + id);
	}
}

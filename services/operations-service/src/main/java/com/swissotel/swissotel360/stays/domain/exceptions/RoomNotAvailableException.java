package com.swissotel.swissotel360.stays.domain.exceptions;

public class RoomNotAvailableException extends RuntimeException {
	public RoomNotAvailableException(String reason) {
		super("Room not available: " + reason);
	}
}

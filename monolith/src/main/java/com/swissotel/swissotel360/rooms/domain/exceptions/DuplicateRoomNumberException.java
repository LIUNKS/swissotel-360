package com.swissotel.swissotel360.rooms.domain.exceptions;

public class DuplicateRoomNumberException extends RuntimeException {
	public DuplicateRoomNumberException(String value) {
		super("Room number already exists: " + value);
	}
}

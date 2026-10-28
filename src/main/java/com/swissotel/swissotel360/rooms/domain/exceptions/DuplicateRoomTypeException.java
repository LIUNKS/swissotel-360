package com.swissotel.swissotel360.rooms.domain.exceptions;
public class DuplicateRoomTypeException extends RuntimeException { public DuplicateRoomTypeException(String name) { super("Room type already exists: " + name); } }

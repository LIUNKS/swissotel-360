package com.swissotel.swissotel360.rooms.domain.exceptions;
import java.util.UUID;
public class RoomInUseException extends RuntimeException { public RoomInUseException(UUID id) { super("Room has associated stays: " + id); } }

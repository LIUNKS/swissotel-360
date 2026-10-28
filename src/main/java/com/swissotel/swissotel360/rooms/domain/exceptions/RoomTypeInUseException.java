package com.swissotel.swissotel360.rooms.domain.exceptions;
import java.util.UUID;
public class RoomTypeInUseException extends RuntimeException { public RoomTypeInUseException(UUID id) { super("Room type is in use: " + id); } }

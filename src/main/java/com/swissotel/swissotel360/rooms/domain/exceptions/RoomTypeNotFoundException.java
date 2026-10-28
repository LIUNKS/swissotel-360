package com.swissotel.swissotel360.rooms.domain.exceptions;
import java.util.UUID;
public class RoomTypeNotFoundException extends RuntimeException { public RoomTypeNotFoundException(UUID id) { super("Room type not found: " + id); } }

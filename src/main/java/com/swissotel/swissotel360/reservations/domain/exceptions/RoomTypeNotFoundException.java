package com.swissotel.swissotel360.reservations.domain.exceptions;

import java.util.UUID;
public class RoomTypeNotFoundException extends RuntimeException {
    public RoomTypeNotFoundException(UUID id) { super("Room type not found: " + id); }
    public RoomTypeNotFoundException(Long id) { super("Room type not found: " + id); }
}

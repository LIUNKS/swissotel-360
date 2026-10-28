package com.swissotel.swissotel360.rooms.domain.exceptions;
import java.util.UUID;
public class OccupiedRoomTypeChangeException extends RuntimeException { public OccupiedRoomTypeChangeException(UUID roomPublicId) { super("Room type cannot change while the room is occupied: " + roomPublicId); } }

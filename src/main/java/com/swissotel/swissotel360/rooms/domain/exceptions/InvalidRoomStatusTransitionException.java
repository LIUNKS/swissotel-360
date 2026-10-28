package com.swissotel.swissotel360.rooms.domain.exceptions;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
public class InvalidRoomStatusTransitionException extends RuntimeException { private final RoomStatus from; private final RoomStatus to; public InvalidRoomStatusTransitionException(RoomStatus from, RoomStatus to) { super("Invalid room status transition: " + from + " -> " + to); this.from = from; this.to = to; } public RoomStatus getFrom() { return from; } public RoomStatus getTo() { return to; } }

package com.swissotel.swissotel360.rooms.domain.model.aggregates;
import com.swissotel.swissotel360.rooms.domain.exceptions.InvalidRoomStatusTransitionException;
import com.swissotel.swissotel360.rooms.domain.exceptions.OccupiedRoomTypeChangeException;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import java.util.UUID;
public record Room(Long id, UUID publicId, Long roomTypeId, String roomNumber, Short floor, RoomStatus status) {
    public Room {
        if (publicId == null || roomTypeId == null || floor == null || status == null) throw new IllegalArgumentException("Room identity, type, floor and status are required");
        if (roomNumber == null || roomNumber.isBlank() || roomNumber.trim().length() > 10) throw new IllegalArgumentException("roomNumber must contain 1 to 10 characters");
        roomNumber = roomNumber.trim();
    }
    public Room changeDetails(Long newRoomTypeId, Short newFloor) {
        if (status == RoomStatus.OCCUPIED && !roomTypeId.equals(newRoomTypeId)) throw new OccupiedRoomTypeChangeException(publicId);
        return new Room(id, publicId, newRoomTypeId, roomNumber, newFloor, status);
    }
    public Room transitionTo(RoomStatus next) {
        boolean allowed = switch (status) {
            case AVAILABLE -> next == RoomStatus.OUT_OF_SERVICE;
            case OCCUPIED -> next == RoomStatus.CLEANING;
            case CLEANING -> next == RoomStatus.AVAILABLE || next == RoomStatus.OUT_OF_SERVICE;
            case OUT_OF_SERVICE -> next == RoomStatus.AVAILABLE;
        };
        if (!allowed) throw new InvalidRoomStatusTransitionException(status, next);
        return new Room(id, publicId, roomTypeId, roomNumber, floor, next);
    }
    public Room markOccupied() {
        if (status != RoomStatus.AVAILABLE) throw new InvalidRoomStatusTransitionException(status, RoomStatus.OCCUPIED);
        return new Room(id, publicId, roomTypeId, roomNumber, floor, RoomStatus.OCCUPIED);
    }
    public Room markCheckedOut() {
        if (status != RoomStatus.OCCUPIED) throw new InvalidRoomStatusTransitionException(status, RoomStatus.CLEANING);
        return new Room(id, publicId, roomTypeId, roomNumber, floor, RoomStatus.CLEANING);
    }
}

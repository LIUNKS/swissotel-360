package com.swissotel.swissotel360.rooms.interfaces.acl;
import java.time.LocalDate;
import java.util.UUID;
public interface AvailabilityContextFacade {
    int countAvailable(UUID roomTypePublicId,LocalDate checkIn,LocalDate checkOut);
    RoomAvailabilitySnapshot quote(UUID roomTypePublicId,LocalDate checkIn,LocalDate checkOut);
}

package com.swissotel.swissotel360.reservations.interfaces.acl;
import java.time.LocalDate;
import java.util.UUID;
public interface ReservationsContextFacade {
    long countActiveReservationsOverlapping(UUID roomTypePublicId, LocalDate checkIn, LocalDate checkOut);
    boolean hasReservationsForRoomType(UUID roomTypePublicId);
}

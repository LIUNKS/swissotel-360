package com.swissotel.swissotel360.rooms.application.internal.outboundservices.acl;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationsContextFacade;
import java.time.LocalDate;
public class ReservationsAvailabilityOutboundService implements ReservationsAvailabilityService {
    private final ReservationsContextFacade reservations;
    public ReservationsAvailabilityOutboundService(ReservationsContextFacade reservations) { this.reservations=reservations; }
    public long countOverlappingActiveReservations(Long id,LocalDate checkIn,LocalDate checkOut) { return reservations.countActiveReservationsOverlapping(id,checkIn,checkOut); }
    public boolean hasReservations(Long id) { return reservations.hasReservationsForRoomType(id); }
}

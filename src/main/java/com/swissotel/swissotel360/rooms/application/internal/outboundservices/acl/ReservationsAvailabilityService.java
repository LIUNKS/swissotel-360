package com.swissotel.swissotel360.rooms.application.internal.outboundservices.acl;
import java.time.LocalDate;
public interface ReservationsAvailabilityService {
    long countOverlappingActiveReservations(Long roomTypeId,LocalDate checkIn,LocalDate checkOut);
    boolean hasReservations(Long roomTypeId);
}

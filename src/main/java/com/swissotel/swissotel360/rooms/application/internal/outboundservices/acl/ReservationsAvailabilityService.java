package com.swissotel.swissotel360.rooms.application.internal.outboundservices.acl;
import java.time.LocalDate;
import java.util.UUID;
public interface ReservationsAvailabilityService {
    long countOverlappingActiveReservations(UUID roomTypePublicId,LocalDate checkIn,LocalDate checkOut);
    boolean hasReservations(UUID roomTypePublicId);
}

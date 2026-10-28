package com.swissotel.swissotel360.reservations.application.internal.queryservices;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationAvailabilityRepository;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationsContextFacade;
import java.time.LocalDate;
import java.util.UUID;
public class ReservationsContextFacadeImpl implements ReservationsContextFacade {
    private final ReservationAvailabilityRepository repository;
    public ReservationsContextFacadeImpl(ReservationAvailabilityRepository repository) { this.repository=repository; }
    public long countActiveReservationsOverlapping(UUID id,LocalDate checkIn,LocalDate checkOut) { return repository.countActiveOverlapping(id,checkIn,checkOut); }
    public boolean hasReservationsForRoomType(UUID id) { return repository.hasReservations(id); }
}

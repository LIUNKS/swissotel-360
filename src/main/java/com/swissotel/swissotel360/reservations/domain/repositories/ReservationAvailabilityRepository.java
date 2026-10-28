package com.swissotel.swissotel360.reservations.domain.repositories;
import java.time.LocalDate;
import java.util.UUID;
public interface ReservationAvailabilityRepository { long countActiveOverlapping(UUID roomTypePublicId,LocalDate checkIn,LocalDate checkOut); boolean hasReservations(UUID roomTypePublicId); }

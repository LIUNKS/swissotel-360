package com.swissotel.swissotel360.reservations.domain.repositories;
import java.time.LocalDate;
public interface ReservationAvailabilityRepository { long countActiveOverlapping(Long roomTypeId,LocalDate checkIn,LocalDate checkOut); boolean hasReservations(Long roomTypeId); }

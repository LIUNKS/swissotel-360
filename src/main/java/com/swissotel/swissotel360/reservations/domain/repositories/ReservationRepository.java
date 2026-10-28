package com.swissotel.swissotel360.reservations.domain.repositories;

import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.model.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
	Reservation save(Reservation reservation);

	Optional<Reservation> findById(ReservationId id);

	Optional<Reservation> findByCode(ReservationCode code);

	long countActiveOverlapping(RoomTypeId type, StayPeriod period, Optional<ReservationId> excluding);

	List<Reservation> findPendingCreatedBefore(Instant threshold, PaymentType type, int limit);

	boolean existsByCode(ReservationCode code);

	ReservationPage search(ReservationSearch search);
}

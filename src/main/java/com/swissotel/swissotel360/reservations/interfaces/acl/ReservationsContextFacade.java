package com.swissotel.swissotel360.reservations.interfaces.acl;

import java.time.LocalDate;
import java.util.UUID;

public interface ReservationsContextFacade {
	long countActiveReservationsOverlapping(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);

	boolean hasReservationsForRoomType(Long roomTypeId);

	ReservationContextSnapshot getForCheckIn(UUID reservationPublicId);

	java.util.Optional<ReservationContextSnapshot> findByCode(String code);

	void markCompleted(UUID reservationPublicId);

	void confirmPrepaid(UUID reservationPublicId);
}

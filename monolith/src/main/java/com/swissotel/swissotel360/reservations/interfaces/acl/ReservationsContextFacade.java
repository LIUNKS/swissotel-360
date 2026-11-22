package com.swissotel.swissotel360.reservations.interfaces.acl;

import java.time.LocalDate;

public interface ReservationsContextFacade {
	long countActiveReservationsOverlapping(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);

	boolean hasReservationsForRoomType(Long roomTypeId);

	ReservationContextSnapshot getForCheckIn(Long reservationId);

	java.util.Optional<ReservationContextSnapshot> findByCode(String code);

	void markCompleted(Long reservationId);

	void confirmPrepaid(Long reservationId);
}

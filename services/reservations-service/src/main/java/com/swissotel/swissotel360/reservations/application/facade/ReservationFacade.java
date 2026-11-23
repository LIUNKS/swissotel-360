package com.swissotel.swissotel360.reservations.application.facade;

import com.swissotel.swissotel360.reservations.application.dto.ReservationSnapshot;

public interface ReservationFacade {
	ReservationSnapshot getForCheckIn(Long reservationId);

	java.util.Optional<ReservationSnapshot> findByCode(String code);

	void markCompleted(Long reservationId);

	void confirmPrepaid(Long reservationId);
}

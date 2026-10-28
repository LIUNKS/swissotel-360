package com.swissotel.swissotel360.reservations.application.facade;

import com.swissotel.swissotel360.reservations.application.dto.ReservationSnapshot;
import java.util.UUID;

public interface ReservationFacade {
	ReservationSnapshot getForCheckIn(UUID reservationPublicId);

	void markCompleted(UUID reservationPublicId);

	void confirmPrepaid(UUID reservationPublicId);
}

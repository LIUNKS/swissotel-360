package com.swissotel.swissotel360.reservations.application.commandservices;

import com.swissotel.swissotel360.reservations.application.command.*;
import com.swissotel.swissotel360.reservations.application.dto.ReservationResult;
import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;

public interface ReservationCommandService {
	ReservationResult create(CreateReservationCommand command);

	ReservationResult modify(ModifyReservationCommand command);

	Reservation confirm(ConfirmReservationCommand command);

	Reservation cancel(CancelReservationCommand command);

	Reservation complete(CompleteReservationCommand command);
}

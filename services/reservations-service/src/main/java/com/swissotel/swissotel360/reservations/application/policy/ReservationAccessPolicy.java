package com.swissotel.swissotel360.reservations.application.policy;

import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort.CurrentActor;
import com.swissotel.swissotel360.reservations.application.port.GuestDirectoryPort;
import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.exceptions.GuestProfileRequiredException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotFoundException;

public final class ReservationAccessPolicy {
	public void assertCanAccess(CurrentActor actor, Reservation reservation, GuestDirectoryPort guests) {
		if (!actor.isGuest())
			return;
		var ownGuest = guests.findByUserId(actor.userId())
				.orElseThrow(() -> new ReservationNotFoundException(reservation.id().value()));
		if (!ownGuest.id().equals(reservation.guestId()))
			throw new ReservationNotFoundException(reservation.id().value());
	}
}

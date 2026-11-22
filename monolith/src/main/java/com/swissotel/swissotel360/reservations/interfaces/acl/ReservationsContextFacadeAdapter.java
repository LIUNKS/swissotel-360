package com.swissotel.swissotel360.reservations.interfaces.acl;

import com.swissotel.swissotel360.reservations.application.facade.ReservationFacade;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationAvailabilityRepository;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;
import java.time.LocalDate;

@Component
public class ReservationsContextFacadeAdapter implements ReservationsContextFacade {
	private final ReservationAvailabilityRepository availability;
	private final ReservationFacade reservations;

	public ReservationsContextFacadeAdapter(ReservationAvailabilityRepository availability,
			@Lazy ReservationFacade reservations) {
		this.availability = availability;
		this.reservations = reservations;
	}

	@Override
	public long countActiveReservationsOverlapping(Long type, LocalDate in, LocalDate out) {
		return availability.countActiveOverlapping(type, in, out);
	}

	@Override
	public boolean hasReservationsForRoomType(Long type) {
		return availability.hasReservations(type);
	}

	@Override
	public ReservationContextSnapshot getForCheckIn(Long reservationId) {
		return toSnapshot(reservations.getForCheckIn(reservationId));
	}

	@Override
	public java.util.Optional<ReservationContextSnapshot> findByCode(String code) {
		return reservations.findByCode(code).map(ReservationsContextFacadeAdapter::toSnapshot);
	}

	private static ReservationContextSnapshot toSnapshot(
			com.swissotel.swissotel360.reservations.application.dto.ReservationSnapshot s) {
		return new ReservationContextSnapshot(s.publicId(), s.code(), s.guestId().value(), s.roomTypeId().value(),
				s.checkIn(), s.checkOut(), s.status(), s.internalId());
	}

	@Override
	public void markCompleted(Long reservationId) {
		reservations.markCompleted(reservationId);
	}

	@Override
	public void confirmPrepaid(Long reservationId) {
		reservations.confirmPrepaid(reservationId);
	}
}

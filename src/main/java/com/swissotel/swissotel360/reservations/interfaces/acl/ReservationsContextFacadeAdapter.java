package com.swissotel.swissotel360.reservations.interfaces.acl;

import com.swissotel.swissotel360.reservations.application.facade.ReservationFacade;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationAvailabilityRepository;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;
import java.time.LocalDate;
import java.util.UUID;

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
	public ReservationContextSnapshot getForCheckIn(UUID id) {
		var s = reservations.getForCheckIn(id);
		return new ReservationContextSnapshot(s.publicId(), s.code(), s.guestId().value(), s.roomTypeId().value(),
				s.checkIn(), s.checkOut(), s.status());
	}

	@Override
	public void markCompleted(UUID id) {
		reservations.markCompleted(id);
	}

	@Override
	public void confirmPrepaid(UUID id) {
		reservations.confirmPrepaid(id);
	}
}

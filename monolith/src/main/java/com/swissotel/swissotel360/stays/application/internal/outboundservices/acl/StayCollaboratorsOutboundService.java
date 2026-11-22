package com.swissotel.swissotel360.stays.application.internal.outboundservices.acl;

import com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectoryContextFacade;
import com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectorySnapshot;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationContextSnapshot;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationsContextFacade;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomInfoSnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomsContextFacade;
import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

/** Outbound adapter: the only place where stays talks to other bounded contexts. */
@Component
public class StayCollaboratorsOutboundService {
	private final ReservationsContextFacade reservations;
	private final GuestDirectoryContextFacade guests;
	private final RoomsContextFacade rooms;

	public StayCollaboratorsOutboundService(ReservationsContextFacade reservations,
			GuestDirectoryContextFacade guests, RoomsContextFacade rooms) {
		this.reservations = reservations;
		this.guests = guests;
		this.rooms = rooms;
	}

	public Optional<ReservationContextSnapshot> findReservation(String code) {
		return reservations.findByCode(code);
	}

	public void completeReservation(Long reservationId) {
		reservations.markCompleted(reservationId);
	}

	public Optional<GuestDirectorySnapshot> findGuest(Long guestId) {
		return guests.findById(guestId);
	}

	public Optional<RoomInfoSnapshot> findRoom(UUID roomPublicId) {
		return rooms.findByPublicId(roomPublicId);
	}

	public Optional<RoomInfoSnapshot> findRoom(Long roomId) {
		return rooms.findById(roomId);
	}

	public Optional<RoomInfoSnapshot> findAvailableRoom(Long roomTypeId) {
		return rooms.findFirstAvailable(roomTypeId);
	}

	public void markRoomOccupied(Long roomId) {
		rooms.markOccupied(roomId);
	}

	public void markRoomCheckedOut(Long roomId) {
		rooms.markCheckedOut(roomId);
	}

	/** Internal IAM id of the authenticated user. */
	public Long currentUserId() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal))
			throw new IllegalStateException("An authenticated user is required");
		return principal.userId();
	}
}

package com.swissotel.swissotel360.stays.application.internal.outboundservices.acl;

import com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectoryContextFacade;
import com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectorySnapshot;
import com.swissotel.swissotel360.iam.interfaces.acl.IamContextFacade;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationContextSnapshot;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationsContextFacade;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomInfoSnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomsContextFacade;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

/** Outbound adapter: the only place where stays talks to other bounded contexts. */
@Component
public class StayCollaboratorsOutboundService {
	private final ReservationsContextFacade reservations;
	private final GuestDirectoryContextFacade guests;
	private final RoomsContextFacade rooms;
	private final IamContextFacade iam;

	public StayCollaboratorsOutboundService(ReservationsContextFacade reservations,
			GuestDirectoryContextFacade guests, RoomsContextFacade rooms, IamContextFacade iam) {
		this.reservations = reservations;
		this.guests = guests;
		this.rooms = rooms;
		this.iam = iam;
	}

	public Optional<ReservationContextSnapshot> findReservation(String code) {
		return reservations.findByCode(code);
	}

	public void completeReservation(UUID reservationPublicId) {
		reservations.markCompleted(reservationPublicId);
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

	public void markRoomOccupied(UUID roomPublicId) {
		rooms.markOccupied(roomPublicId);
	}

	public void markRoomCheckedOut(UUID roomPublicId) {
		rooms.markCheckedOut(roomPublicId);
	}

	/** Internal IAM id of the authenticated user, or null when it cannot be resolved. */
	public Long currentUserId() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails principal))
			throw new IllegalStateException("An authenticated user is required");
		var id = iam.fetchUserIdByEmail(principal.getUsername());
		return id == null || id < 1 ? null : id;
	}
}

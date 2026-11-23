package com.swissotel.swissotel360.operations.infrastructure.http;

import com.swissotel.swissotel360.shared.infrastructure.security.AuthenticatedUserPrincipal;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayCollaboratorsOutboundService;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayGuestSnapshot;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayReservationSnapshot;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayRoomSnapshot;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class HttpStayCollaboratorsAdapter implements StayCollaboratorsOutboundService {

	private final ReservationsInternalClient reservations;
	private final GuestsInternalClient guests;
	private final RoomsInternalClient rooms;

	public HttpStayCollaboratorsAdapter(
			ReservationsInternalClient reservations,
			GuestsInternalClient guests,
			RoomsInternalClient rooms) {
		this.reservations = reservations;
		this.guests = guests;
		this.rooms = rooms;
	}

	@Override
	public Optional<StayReservationSnapshot> findReservation(String code) {
		return getOrEmpty(() -> reservations.findByCode(code));
	}

	@Override
	public void completeReservation(Long reservationId) {
		reservations.markCompleted(reservationId);
	}

	@Override
	public Optional<StayGuestSnapshot> findGuest(Long guestId) {
		return getOrEmpty(() -> guests.getById(guestId));
	}

	@Override
	public Optional<StayRoomSnapshot> findRoom(UUID roomPublicId) {
		return getOrEmpty(() -> rooms.getByPublicId(roomPublicId));
	}

	@Override
	public Optional<StayRoomSnapshot> findRoom(Long roomId) {
		return getOrEmpty(() -> rooms.getById(roomId));
	}

	@Override
	public Optional<StayRoomSnapshot> findAvailableRoom(Long roomTypeId) {
		return getOrEmpty(() -> rooms.findAvailable(roomTypeId));
	}

	@Override
	public void markRoomOccupied(Long roomId) {
		rooms.occupy(roomId);
	}

	@Override
	public void markRoomCheckedOut(Long roomId) {
		rooms.checkOut(roomId);
	}

	@Override
	public void releaseRoom(Long roomId) {
		rooms.release(roomId);
	}

	@Override
	public Long currentUserId() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal))
			throw new IllegalStateException("An authenticated user is required");
		return principal.userId();
	}

	private static <T> Optional<T> getOrEmpty(Supplier<T> call) {
		try {
			return Optional.ofNullable(call.get());
		} catch (HttpClientErrorException.NotFound ignored) {
			return Optional.empty();
		}
	}
}

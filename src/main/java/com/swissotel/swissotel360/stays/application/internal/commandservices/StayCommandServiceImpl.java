package com.swissotel.swissotel360.stays.application.internal.commandservices;

import com.swissotel.swissotel360.stays.application.command.*;
import com.swissotel.swissotel360.stays.application.commandservices.StayCommandService;
import com.swissotel.swissotel360.stays.application.dto.StayView;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayCollaboratorsOutboundService;
import com.swissotel.swissotel360.stays.application.port.StayEventPublisher;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.exceptions.*;
import com.swissotel.swissotel360.stays.domain.model.DigitalKey;
import com.swissotel.swissotel360.stays.domain.model.StayId;
import com.swissotel.swissotel360.stays.domain.repositories.StayRepository;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationContextSnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomInfoSnapshot;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

public final class StayCommandServiceImpl implements StayCommandService {
	private final StayRepository stays;
	private final StayCollaboratorsOutboundService collaborators;
	private final StayEventPublisher events;
	private final Clock clock;

	public StayCommandServiceImpl(StayRepository stays, StayCollaboratorsOutboundService collaborators,
			StayEventPublisher events, Clock clock) {
		this.stays = stays;
		this.collaborators = collaborators;
		this.events = events;
		this.clock = clock;
	}

	@Override
	public StayView checkIn(CheckInCommand command) {
		var reservation = collaborators.findReservation(command.reservationCode())
				.orElseThrow(() -> new StayReservationNotFoundException(command.reservationCode()));
		requireEligible(reservation);
		if (stays.existsByReservationId(reservation.internalId()))
			throw new ReservationNotEligibleForCheckInException("already checked in");
		var guest = collaborators.findGuest(reservation.guestId())
				.orElseThrow(() -> new GuestDocumentMismatchException());
		if (!command.documentType().equalsIgnoreCase(guest.documentTypeCode())
				|| !command.documentNumber().trim().equals(guest.documentNumber()))
			throw new GuestDocumentMismatchException();
		var room = pickRoom(command, reservation);

		var stay = Stay.checkIn(reservation.internalId(), room.id(), reservation.guestId(),
				collaborators.currentUserId(), Instant.now(clock));
		var saved = stays.save(stay);
		collaborators.markRoomOccupied(room.id());
		// ponytail: reservation is completed at check-in (it cannot be cancelled or re-used afterwards)
		collaborators.completeReservation(reservation.internalId());
		events.publishAfterCommit(saved, stay.domainEvents());
		return new StayView(saved, room.publicId(), room.roomNumber());
	}

	@Override
	public Stay checkOut(CheckOutCommand command) {
		var stay = find(command.stayPublicId());
		stay.checkOut(Instant.now(clock));
		var saved = stays.save(stay);
		collaborators.markRoomCheckedOut(stay.roomId());
		events.publishAfterCommit(saved, stay.domainEvents());
		return saved;
	}

	@Override
	public DigitalKey issueKey(IssueDigitalKeyCommand command) {
		var stay = find(command.stayPublicId());
		stay.issueKey(Instant.now(clock));
		return stays.save(stay).activeKey().orElseThrow();
	}

	@Override
	public DigitalKey revokeKey(RevokeDigitalKeyCommand command) {
		var stay = stays.findByDigitalKeyPublicId(command.keyPublicId())
				.orElseThrow(() -> new DigitalKeyNotFoundException(command.keyPublicId()));
		stay.revokeKey(command.keyPublicId(), Instant.now(clock));
		return stays.save(stay).keys().stream().filter(k -> command.keyPublicId().equals(k.publicId())).findFirst()
				.orElseThrow();
	}

	private Stay find(java.util.UUID id) {
		return stays.findByPublicId(new StayId(id)).orElseThrow(() -> new StayNotFoundException(id));
	}

	private void requireEligible(ReservationContextSnapshot reservation) {
		if (!"confirmed".equals(reservation.status()))
			throw new ReservationNotEligibleForCheckInException("status is " + reservation.status());
		var today = LocalDate.now(clock);
		if (today.isBefore(reservation.checkIn()) || !today.isBefore(reservation.checkOut()))
			throw new ReservationNotEligibleForCheckInException("today is outside the reserved dates");
	}

	private RoomInfoSnapshot pickRoom(CheckInCommand command, ReservationContextSnapshot reservation) {
		if (command.roomPublicId() != null) {
			var room = collaborators.findRoom(command.roomPublicId())
					.orElseThrow(() -> new RoomNotAvailableException("room not found"));
			if (!room.isAvailable())
				throw new RoomNotAvailableException("room is " + room.status());
			return room;
		}
		return collaborators.findAvailableRoom(reservation.roomTypeId())
				.orElseThrow(() -> new RoomNotAvailableException("no available room of the reserved type"));
	}
}

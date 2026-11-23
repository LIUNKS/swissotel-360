package com.swissotel.swissotel360.reservations.application.internal.queryservices;

import com.swissotel.swissotel360.reservations.application.dto.*;
import com.swissotel.swissotel360.reservations.application.port.*;
import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort.CurrentActor;
import com.swissotel.swissotel360.reservations.application.query.*;
import com.swissotel.swissotel360.reservations.application.queryservices.ReservationQueryService;
import com.swissotel.swissotel360.reservations.application.policy.ReservationAccessPolicy;
import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.exceptions.*;
import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

public final class ReservationQueryServiceImpl implements ReservationQueryService {
	private final ReservationRepository reservations;
	private final GuestDirectoryPort guests;
	private final RoomInventoryPort rooms;
	private final ReservationAccessPolicy accessPolicy;
	private final Duration pendingHold;

	public ReservationQueryServiceImpl(ReservationRepository reservations, GuestDirectoryPort guests,
			RoomInventoryPort rooms, ReservationAccessPolicy accessPolicy, Duration pendingHold) {
		this.reservations = reservations;
		this.guests = guests;
		this.rooms = rooms;
		this.accessPolicy = accessPolicy;
		this.pendingHold = pendingHold;
	}

	@Override
	public ReservationResult findByPublicId(GetReservationQuery query, CurrentActor actor) {
		var reservation = reservations.findById(new ReservationId(query.reservationPublicId()))
				.orElseThrow(() -> new ReservationNotFoundException(query.reservationPublicId()));
		return detail(reservation, actor);
	}

	@Override
	public ReservationResult findByCode(GetReservationByCodeQuery query, CurrentActor actor) {
		ReservationCode code;
		try {
			code = new ReservationCode(query.code());
		} catch (IllegalArgumentException ex) {
			throw new ReservationNotFoundException(query.code());
		}
		var reservation = reservations.findByCode(code)
				.orElseThrow(() -> new ReservationNotFoundException(query.code()));
		return detail(reservation, actor);
	}

	@Override
	public ReservationSummaryPage search(SearchReservationsQuery query, CurrentActor actor) {
		GuestId guestId = null;
		if (actor.isGuest())
			guestId = guests.findByUserId(actor.userId()).orElseThrow(GuestProfileRequiredException::new).id();
		else if (query.guestPublicId() != null)
			guestId = guests.findByPublicId(query.guestPublicId())
					.orElseThrow(() -> new GuestNotFoundException(query.guestPublicId())).id();
		var page = reservations.search(new ReservationSearch(query.status(), guestId, query.code(), query.checkInFrom(),
				query.checkInTo(), query.page(), query.size()));
		var summaries = new ArrayList<ReservationSummary>();
		for (var reservation : page.content()) {
			var guest = guests.findById(reservation.guestId())
					.orElseThrow(() -> new GuestNotFoundException(reservation.guestId().value()));
			var type = rooms.findRoomTypeById(reservation.roomTypeId())
					.orElseThrow(() -> new RoomTypeNotFoundException(reservation.roomTypeId().value()));
			summaries.add(new ReservationSummary(reservation, guest.publicId(), guest.fullName(), type.publicId(),
					type.name()));
		}
		return new ReservationSummaryPage(summaries, page.totalElements(), query.page(), query.size());
	}

	private ReservationResult detail(Reservation reservation, CurrentActor actor) {
		accessPolicy.assertCanAccess(actor, reservation, guests);
		var guest = guests.findById(reservation.guestId())
				.orElseThrow(() -> new GuestNotFoundException(reservation.guestId().value()));
		var type = rooms.findRoomTypeById(reservation.roomTypeId())
				.orElseThrow(() -> new RoomTypeNotFoundException(reservation.roomTypeId().value()));
		var quote = rooms.quote(type.id(), reservation.period());
		var hold = reservation.status() == ReservationStatus.PENDING ? reservation.createdAt().plus(pendingHold) : null;
		return new ReservationResult(reservation, guest, type, quote, hold, null);
	}
}

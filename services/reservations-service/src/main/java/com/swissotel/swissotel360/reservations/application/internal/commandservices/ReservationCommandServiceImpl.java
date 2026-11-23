package com.swissotel.swissotel360.reservations.application.internal.commandservices;

import com.swissotel.swissotel360.reservations.application.command.*;
import com.swissotel.swissotel360.reservations.application.commandservices.ReservationCommandService;
import com.swissotel.swissotel360.reservations.application.dto.ReservationResult;
import com.swissotel.swissotel360.reservations.application.port.*;
import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort.CurrentActor;
import com.swissotel.swissotel360.reservations.application.port.GuestDirectoryPort.GuestSnapshot;
import com.swissotel.swissotel360.reservations.application.port.RoomInventoryPort.RoomTypeSnapshot;
import com.swissotel.swissotel360.reservations.application.policy.ReservationAccessPolicy;
import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.exceptions.*;
import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationRepository;
import com.swissotel.swissotel360.reservations.domain.services.*;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ReservationCommandServiceImpl implements ReservationCommandService {
	private final CurrentActorPort actors;
	private final GuestDirectoryPort guests;
	private final RoomInventoryPort rooms;
	private final PrepaymentPort prepayments;
	private final ReservationRepository reservations;
	private final ReservationCodeGenerator codes;
	private final BookingPolicy bookingPolicy;
	private final AvailabilityPolicy availabilityPolicy;
	private final ReservationEventPublisher events;
	private final ReservationAccessPolicy accessPolicy;
	private final Clock clock;
	private final Duration pendingHold;

	public ReservationCommandServiceImpl(CurrentActorPort actors, GuestDirectoryPort guests, RoomInventoryPort rooms,
			PrepaymentPort prepayments, ReservationRepository reservations, ReservationCodeGenerator codes,
			BookingPolicy bookingPolicy, AvailabilityPolicy availabilityPolicy, ReservationEventPublisher events,
			ReservationAccessPolicy accessPolicy, Clock clock, Duration pendingHold) {
		this.actors = actors;
		this.guests = guests;
		this.rooms = rooms;
		this.prepayments = prepayments;
		this.reservations = reservations;
		this.codes = codes;
		this.bookingPolicy = bookingPolicy;
		this.availabilityPolicy = availabilityPolicy;
		this.events = events;
		this.accessPolicy = accessPolicy;
		this.clock = clock;
		this.pendingHold = pendingHold;
	}

	@Override
	public ReservationResult create(CreateReservationCommand command) {
		var actor = actors.current();
		GuestSnapshot guest;
		if (actor.isGuest())
			guest = guests.findByUserId(actor.userId()).orElseThrow(GuestProfileRequiredException::new);
		else {
			if (command.guestPublicId() == null)
				throw new IllegalArgumentException("guestPublicId is required for staff reservations");
			guest = guests.findByPublicId(command.guestPublicId())
					.orElseThrow(() -> new GuestNotFoundException(command.guestPublicId()));
		}
		var type = rooms.findRoomType(command.roomTypePublicId())
				.orElseThrow(() -> new RoomTypeNotFoundException(command.roomTypePublicId()));
		var period = new StayPeriod(command.checkInDate(), command.checkOutDate());
		bookingPolicy.validate(period, LocalDate.now(clock));
		reservations.lockRoomType(type.id());
		assertCapacity(type, period, Optional.empty());
		var quote = rooms.quote(type.id(), period);
		var saved = createAndSave(guest.id(), type.id(), period, command.paymentType(), quote.total(), actor.userId());
		var payment = command.paymentType() == PaymentType.IMMEDIATE
				? prepayments.request(saved.id(), saved.code(), saved.totalAmount())
				: null;
		publishEvents(saved);
		var hold = saved.status() == ReservationStatus.PENDING ? saved.createdAt().plus(pendingHold) : null;
		return new ReservationResult(saved, guest, type, quote, hold, payment);
	}

	@Override
	public ReservationResult modify(ModifyReservationCommand command) {
		var actor = actors.current();
		var reservation = find(command.reservationPublicId());
		accessPolicy.assertCanAccess(actor, reservation, guests);
		if (!reservation.isModifiable())
			throw new ReservationNotModifiableException(reservation.id(), reservation.status());
		var type = rooms.findRoomType(command.roomTypePublicId())
				.orElseThrow(() -> new RoomTypeNotFoundException(command.roomTypePublicId()));
		var period = new StayPeriod(command.checkInDate(), command.checkOutDate());
		bookingPolicy.validate(period, LocalDate.now(clock));
		lockTypesInOrder(reservation.roomTypeId(), type.id());
		assertCapacity(type, period, Optional.of(reservation.id()));
		var quote = rooms.quote(type.id(), period);
		reservation.reschedule(type.id(), period, quote.total());
		var saved = reservations.save(reservation);
		publishEvents(saved);
		var guest = guests.findById(saved.guestId())
				.orElseThrow(() -> new GuestNotFoundException(saved.guestId().value()));
		return new ReservationResult(saved, guest, type, quote,
				saved.status() == ReservationStatus.PENDING ? saved.createdAt().plus(pendingHold) : null, null);
	}

	@Override
	public Reservation confirm(ConfirmReservationCommand command) {
		var reservation = find(command.reservationPublicId());
		if (reservation.status() == ReservationStatus.CONFIRMED)
			return reservation;
		reservation.confirm();
		return saveAndPublish(reservation);
	}

	@Override
	public Reservation cancel(CancelReservationCommand command) {
		var actor = actors.current();
		var reservation = find(command.reservationPublicId());
		accessPolicy.assertCanAccess(actor, reservation, guests);
		reservation.cancel(actor.isGuest() ? CancellationReason.GUEST_REQUEST : CancellationReason.STAFF, clock,
				command.note());
		return saveAndPublish(reservation);
	}

	@Override
	public Reservation complete(CompleteReservationCommand command) {
		var reservation = find(command.reservationPublicId());
		if (reservation.status() == ReservationStatus.COMPLETED)
			return reservation;
		reservation.complete();
		return saveAndPublish(reservation);
	}

	public Reservation expire(ReservationId id) {
		var reservation = reservations.findById(id).orElseThrow(() -> new ReservationNotFoundException(id.value()));
		if (reservation.status() != ReservationStatus.PENDING || reservation.paymentType() != PaymentType.IMMEDIATE)
			return reservation;
		reservation.cancel(CancellationReason.PAYMENT_TIMEOUT, clock, null);
		return saveAndPublish(reservation);
	}

	public List<ReservationId> findExpiredIds() {
		var threshold = clock.instant().minus(pendingHold);
		return reservations.findPendingCreatedBefore(threshold, PaymentType.IMMEDIATE, 100).stream()
				.map(Reservation::id).toList();
	}

	private Reservation find(UUID publicId) {
		return reservations.findById(new ReservationId(publicId))
				.orElseThrow(() -> new ReservationNotFoundException(publicId));
	}

	private Reservation saveAndPublish(Reservation reservation) {
		var saved = reservations.save(reservation);
		publishEvents(saved);
		return saved;
	}

	private void publishEvents(Reservation reservation) {
		var pending = List.copyOf(reservation.domainEvents());
		if (!pending.isEmpty()) {
			events.publishAfterCommit(pending);
			reservation.clearDomainEvents();
		}
	}

	private Reservation createAndSave(GuestId guestId, RoomTypeId roomTypeId, StayPeriod period,
			PaymentType paymentType,
			com.swissotel.swissotel360.shared.domain.model.valueobjects.Money total, UserId createdBy) {
		for (int attempt = 0; attempt < 5; attempt++) {
			var candidate = codes.next();
			if (reservations.existsByCode(candidate))
				continue;
			var reservation = Reservation.create(candidate, guestId, roomTypeId, period, paymentType, total, createdBy,
					clock);
			return reservations.save(reservation);
		}
		throw new DuplicateReservationException();
	}

	private void assertCapacity(RoomTypeSnapshot type, StayPeriod period, Optional<ReservationId> excluding) {
		var overlapping = reservations.countActiveOverlapping(type.id(), period, excluding);
		availabilityPolicy.assertAvailable(rooms.sellableUnits(type.id()), overlapping, type.id());
	}

	private void lockTypesInOrder(RoomTypeId previous, RoomTypeId next) {
		List.of(previous, next).stream().distinct().sorted(Comparator.comparing(RoomTypeId::value))
				.forEach(reservations::lockRoomType);
	}
}

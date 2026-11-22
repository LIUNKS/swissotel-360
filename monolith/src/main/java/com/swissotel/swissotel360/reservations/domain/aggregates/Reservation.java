package com.swissotel.swissotel360.reservations.domain.aggregates;

import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidReservationTransitionException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotModifiableException;
import com.swissotel.swissotel360.reservations.domain.events.*;
import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;
import java.time.Clock;
import java.time.Instant;

public final class Reservation extends AbstractDomainAggregateRoot<Reservation> {
	private final ReservationId id;
	private final ReservationCode code;
	private final GuestId guestId;
	private RoomTypeId roomTypeId;
	private StayPeriod period;
	private ReservationStatus status;
	private final PaymentType paymentType;
	private Money totalAmount;
	private final UserId createdBy;
	private final Instant createdAt;
	private Instant cancelledAt;
	private CancellationReason cancellationReason;
	private String cancellationNote;

	private Reservation(ReservationId id, ReservationCode code, GuestId guestId, RoomTypeId roomTypeId,
			StayPeriod period, ReservationStatus status, PaymentType paymentType, Money totalAmount,
			UserId createdBy, Instant createdAt, Instant cancelledAt, CancellationReason cancellationReason,
			String cancellationNote) {
		if (id == null || code == null || guestId == null || roomTypeId == null || period == null || status == null
				|| paymentType == null || totalAmount == null || createdBy == null || createdAt == null) {
			throw new IllegalArgumentException("Reservation fields are required");
		}
		var cancelled = status == ReservationStatus.CANCELLED;
		if (cancelled != (cancelledAt != null) || cancelled != (cancellationReason != null)) {
			throw new IllegalArgumentException("Cancellation data must exist exactly for cancelled reservations");
		}
		this.id = id;
		this.code = code;
		this.guestId = guestId;
		this.roomTypeId = roomTypeId;
		this.period = period;
		this.status = status;
		this.paymentType = paymentType;
		this.totalAmount = totalAmount;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.cancelledAt = cancelledAt;
		this.cancellationReason = cancellationReason;
		this.cancellationNote = cancellationNote;
	}

	public static Reservation create(ReservationCode code, GuestId guestId, RoomTypeId roomTypeId, StayPeriod period,
			PaymentType paymentType, Money total, UserId createdBy, Clock clock) {
		var status = paymentType == PaymentType.IMMEDIATE ? ReservationStatus.PENDING : ReservationStatus.CONFIRMED;
		var reservation = new Reservation(ReservationId.newId(), code, guestId, roomTypeId, period, status,
				paymentType, total, createdBy, Instant.now(clock), null, null, null);
		reservation.registerDomainEvent(
				new ReservationCreated(reservation.id, code, guestId, roomTypeId, period, paymentType, total, status));
		if (status == ReservationStatus.CONFIRMED)
			reservation.registerDomainEvent(new ReservationConfirmed(reservation.id, code, guestId, period));
		return reservation;
	}

	public static Reservation restore(ReservationId id, ReservationCode code, GuestId guestId, RoomTypeId roomTypeId,
			StayPeriod period, ReservationStatus status, PaymentType paymentType, Money total, UserId createdBy,
			Instant createdAt, Instant cancelledAt, CancellationReason cancellationReason, String cancellationNote) {
		return new Reservation(id, code, guestId, roomTypeId, period, status, paymentType, total, createdBy, createdAt,
				cancelledAt, cancellationReason, cancellationNote);
	}

	public void confirm() {
		transitionTo(ReservationStatus.CONFIRMED);
		registerDomainEvent(new ReservationConfirmed(id, code, guestId, period));
	}

	public void cancel(CancellationReason reason, Clock clock, String note) {
		if (reason == null)
			throw new IllegalArgumentException("Cancellation reason is required");
		if (note != null && note.length() > 200)
			throw new IllegalArgumentException("Cancellation note cannot exceed 200 characters");
		var wasPrepaid = paymentType == PaymentType.IMMEDIATE && status == ReservationStatus.CONFIRMED;
		transitionTo(ReservationStatus.CANCELLED);
		cancelledAt = Instant.now(clock);
		cancellationReason = reason;
		cancellationNote = note;
		registerDomainEvent(new ReservationCancelled(id, code, guestId, period, reason, note, wasPrepaid));
	}

	public void complete() {
		transitionTo(ReservationStatus.COMPLETED);
		registerDomainEvent(new ReservationCompleted(id, code, guestId));
	}

	public void reschedule(RoomTypeId newRoomType, StayPeriod newPeriod, Money newTotal) {
		if (!isModifiable())
			throw new ReservationNotModifiableException(id, status);
		if (newRoomType == null || newPeriod == null || newTotal == null)
			throw new IllegalArgumentException("New room type, period and total are required");
		roomTypeId = newRoomType;
		period = newPeriod;
		totalAmount = newTotal;
		registerDomainEvent(new ReservationRescheduled(id, code, guestId, newRoomType, newPeriod, newTotal));
	}

	private void transitionTo(ReservationStatus target) {
		if (!status.canTransitionTo(target))
			throw new InvalidReservationTransitionException(id, status, target);
		status = target;
	}

	public boolean isActive() {
		return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
	}

	public boolean isModifiable() {
		return status == ReservationStatus.PENDING
				|| (status == ReservationStatus.CONFIRMED && paymentType == PaymentType.DEFERRED);
	}

	public ReservationId id() {
		return id;
	}

	public ReservationCode code() {
		return code;
	}

	public GuestId guestId() {
		return guestId;
	}

	public RoomTypeId roomTypeId() {
		return roomTypeId;
	}

	public StayPeriod period() {
		return period;
	}

	public ReservationStatus status() {
		return status;
	}

	public PaymentType paymentType() {
		return paymentType;
	}

	public Money totalAmount() {
		return totalAmount;
	}

	public UserId createdBy() {
		return createdBy;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant cancelledAt() {
		return cancelledAt;
	}

	public CancellationReason cancellationReason() {
		return cancellationReason;
	}

	public String cancellationNote() {
		return cancellationNote;
	}
}

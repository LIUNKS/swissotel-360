package com.swissotel.swissotel360.reservations.domain.exceptions;

import com.swissotel.swissotel360.reservations.domain.model.ReservationId;
import com.swissotel.swissotel360.reservations.domain.model.ReservationStatus;
public class InvalidReservationTransitionException extends RuntimeException {
    private final ReservationStatus from; private final ReservationStatus to;
    public InvalidReservationTransitionException(ReservationId id, ReservationStatus from, ReservationStatus to) { super("Reservation " + id.value() + " cannot transition from " + from + " to " + to); this.from=from; this.to=to; }
    public ReservationStatus getFrom() { return from; } public ReservationStatus getTo() { return to; }
}

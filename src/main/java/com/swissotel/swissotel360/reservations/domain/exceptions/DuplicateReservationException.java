package com.swissotel.swissotel360.reservations.domain.exceptions;

public class DuplicateReservationException extends RuntimeException {
    public DuplicateReservationException() { super("An active reservation already exists for this guest and stay"); }
}

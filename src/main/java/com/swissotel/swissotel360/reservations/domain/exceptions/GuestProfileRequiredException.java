package com.swissotel.swissotel360.reservations.domain.exceptions;

public class GuestProfileRequiredException extends RuntimeException {
    public GuestProfileRequiredException() { super("The authenticated guest must have a guest profile"); }
}

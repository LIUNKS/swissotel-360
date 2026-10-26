package com.swissotel.swissotel360.guests.domain.exceptions;

public class GuestNotFoundException extends RuntimeException {
    public GuestNotFoundException(String identifier) { super("Guest not found: " + identifier); }
}

package com.swissotel.swissotel360.guests.domain.exceptions;

public class DuplicateGuestException extends RuntimeException {
    public DuplicateGuestException() { super("A guest with this document already exists"); }
}

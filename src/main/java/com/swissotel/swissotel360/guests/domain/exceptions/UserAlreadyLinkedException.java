package com.swissotel.swissotel360.guests.domain.exceptions;

public class UserAlreadyLinkedException extends RuntimeException {
    public UserAlreadyLinkedException() { super("This IAM account is already linked to a guest"); }
}

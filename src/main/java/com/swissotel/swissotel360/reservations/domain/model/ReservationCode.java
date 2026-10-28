package com.swissotel.swissotel360.reservations.domain.model;

import java.util.regex.Pattern;

public record ReservationCode(String value) {
    private static final Pattern FORMAT = Pattern.compile("^SW\\d{2}-[A-HJ-NP-Z2-9]{8}$");
    public ReservationCode {
        if (value == null || !FORMAT.matcher(value).matches()) throw new IllegalArgumentException("Invalid reservation code");
    }
}

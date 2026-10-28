package com.swissotel.swissotel360.reservations.domain.model;

import java.time.LocalDate;

public record ReservationSearch(ReservationStatus status, GuestId guestId, String code,
        LocalDate checkInFrom, LocalDate checkInTo, int page, int size) {
    public ReservationSearch {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page and size are invalid");
        if (checkInFrom != null && checkInTo != null && checkInTo.isBefore(checkInFrom)) throw new IllegalArgumentException("checkInTo cannot be before checkInFrom");
        code = code == null || code.isBlank() ? null : code.trim();
    }
}

package com.swissotel.swissotel360.reservations.domain.events;

import com.swissotel.swissotel360.reservations.domain.model.*;

public record ReservationConfirmed(ReservationId id, ReservationCode code, GuestId guestId, StayPeriod period) {
}

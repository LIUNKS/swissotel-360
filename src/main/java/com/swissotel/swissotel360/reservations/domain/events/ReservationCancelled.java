package com.swissotel.swissotel360.reservations.domain.events;

import com.swissotel.swissotel360.reservations.domain.model.*;

public record ReservationCancelled(ReservationId id, ReservationCode code, GuestId guestId, StayPeriod period,
		CancellationReason reason, String note, boolean wasPrepaid) {
}

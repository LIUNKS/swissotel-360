package com.swissotel.swissotel360.stays.domain.events;

import com.swissotel.swissotel360.stays.domain.model.StayId;

public record StayCheckedIn(StayId id, Long reservationId, Long guestId, Long roomId) {
}

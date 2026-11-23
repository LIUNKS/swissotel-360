package com.swissotel.swissotel360.stays.domain.events;

import com.swissotel.swissotel360.stays.domain.model.StayId;

public record StayCheckedOut(StayId id, Long guestId, Long roomId) {
}

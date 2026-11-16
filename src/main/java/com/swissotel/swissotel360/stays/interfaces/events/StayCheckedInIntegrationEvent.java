package com.swissotel.swissotel360.stays.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/** Public event emitted after a check-in commits. Ids are internal database ids except stayPublicId. */
public record StayCheckedInIntegrationEvent(UUID stayPublicId, Long stayId, Long reservationId, Long guestId,
		Long roomId, Instant checkedInAt) {
}

package com.swissotel.swissotel360.stays.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/** Public event emitted after a check-out commits. Ids are internal database ids except stayPublicId. */
public record StayCheckedOutIntegrationEvent(UUID stayPublicId, Long stayId, Long guestId, Long roomId,
		Instant checkedOutAt) {
}

package com.swissotel.swissotel360.billing.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/** Consumer-owned integration event contract for check-ins published to swissotel.events. */
public record StayCheckedInEventMessage(
		UUID stayPublicId,
		Long stayId,
		Long reservationId,
		Long guestId,
		Long roomId,
		Instant checkedInAt) {
}

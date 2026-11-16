package com.swissotel.swissotel360.stays.interfaces.acl;

import java.time.Instant;
import java.util.UUID;

/** status is "active" or "closed". */
public record StayContextSnapshot(Long id, UUID publicId, Long reservationId, Long guestId, Long roomId,
		String status, Instant checkedInAt, Instant checkedOutAt) {
}

package com.swissotel.swissotel360.billing.application.internal.outboundservices.acl;

import java.time.Instant;
import java.util.UUID;

/** Read-only stay collaborator snapshot decoupled from stays bounded context. */
public record StayCollaboratorSnapshot(
		Long id,
		UUID publicId,
		Long reservationId,
		Long guestId,
		Long roomId,
		String status,
		Instant checkedInAt,
		Instant checkedOutAt) {
}

package com.swissotel.swissotel360.stays.application.internal.outboundservices.acl;

import java.util.UUID;

public record StayGuestSnapshot(
		Long id,
		UUID publicId,
		Long userId,
		String fullName,
		String email,
		String documentTypeCode,
		String documentNumber) {
}

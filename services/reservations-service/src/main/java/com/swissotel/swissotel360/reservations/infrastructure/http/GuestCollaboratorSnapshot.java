package com.swissotel.swissotel360.reservations.infrastructure.http;

import java.util.UUID;

public record GuestCollaboratorSnapshot(
		Long id,
		UUID publicId,
		Long userId,
		String fullName,
		String email,
		String documentTypeCode,
		String documentNumber) {
}

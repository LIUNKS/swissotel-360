package com.swissotel.swissotel360.reservations.infrastructure.http;

import java.util.UUID;

public record RoomTypeCollaboratorSnapshot(
		Long id,
		UUID publicId,
		String name,
		short capacity) {
}

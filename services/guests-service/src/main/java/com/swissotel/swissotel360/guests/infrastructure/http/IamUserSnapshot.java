package com.swissotel.swissotel360.guests.infrastructure.http;

import java.util.UUID;

public record IamUserSnapshot(
		Long id,
		UUID publicId,
		String email,
		String fullName,
		String role,
		String status) {
}

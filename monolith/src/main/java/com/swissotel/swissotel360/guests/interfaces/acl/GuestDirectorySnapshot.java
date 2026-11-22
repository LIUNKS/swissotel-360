package com.swissotel.swissotel360.guests.interfaces.acl;

import java.util.UUID;

public record GuestDirectorySnapshot(Long id, UUID publicId, Long userId, String fullName, String email,
		String documentTypeCode, String documentNumber) {
}

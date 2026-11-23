package com.swissotel.swissotel360.stays.application.internal.outboundservices.acl;

import java.util.UUID;

public record StayRoomSnapshot(
		Long id,
		UUID publicId,
		Long roomTypeId,
		String roomNumber,
		String status) {

	public boolean isAvailable() {
		return "AVAILABLE".equals(status);
	}
}

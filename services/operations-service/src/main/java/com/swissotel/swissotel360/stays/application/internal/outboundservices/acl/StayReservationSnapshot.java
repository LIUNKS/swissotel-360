package com.swissotel.swissotel360.stays.application.internal.outboundservices.acl;

import java.time.LocalDate;
import java.util.UUID;

public record StayReservationSnapshot(
		UUID publicId,
		String code,
		Long guestId,
		Long roomTypeId,
		LocalDate checkIn,
		LocalDate checkOut,
		String status,
		Long internalId) {
}

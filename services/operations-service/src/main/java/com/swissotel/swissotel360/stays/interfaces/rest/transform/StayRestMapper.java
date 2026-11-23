package com.swissotel.swissotel360.stays.interfaces.rest.transform;

import com.swissotel.swissotel360.stays.application.command.*;
import com.swissotel.swissotel360.stays.application.dto.StayView;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.model.DigitalKey;
import com.swissotel.swissotel360.stays.interfaces.rest.resources.*;
import java.util.UUID;

public final class StayRestMapper {
	private StayRestMapper() {
	}

	public static CheckInCommand toCommand(CheckInStayResource r) {
		return new CheckInCommand(r.reservationCode().trim(), r.documentType().trim(), r.documentNumber(), r.roomId());
	}

	public static CheckInResultResource toCheckIn(StayView v) {
		var s = v.stay();
		return new CheckInResultResource(s.id().value(), v.roomNumber(),
				s.activeKey().map(DigitalKey::accessCode).orElse(null), s.status().code(), s.checkedInAt());
	}

	public static StayResource toResource(StayView v) {
		var s = v.stay();
		return new StayResource(s.id().value(), v.roomPublicId(), v.roomNumber(), s.status().code(), s.checkedInAt(),
				s.checkedOutAt());
	}

	public static CheckOutResource toCheckOut(Stay s) {
		return new CheckOutResource(s.id().value(), s.status().code(), s.checkedOutAt());
	}

	public static IssueDigitalKeyCommand toCommand(IssueDigitalKeyResource r) {
		return new IssueDigitalKeyCommand(r.stayId());
	}

	public static DigitalKeyResource toIssued(DigitalKey k) {
		return new DigitalKeyResource(k.publicId(), k.accessCode(), k.status().code());
	}

	public static DigitalKeyResource toRevoked(DigitalKey k) {
		return new DigitalKeyResource(k.publicId(), null, k.status().code());
	}

	public static CheckOutCommand checkOut(UUID id) {
		return new CheckOutCommand(id);
	}
}

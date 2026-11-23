package com.swissotel.swissotel360.reservations.interfaces.rest.transform;

import com.swissotel.swissotel360.reservations.application.command.*;
import com.swissotel.swissotel360.reservations.application.dto.*;
import com.swissotel.swissotel360.reservations.domain.model.PaymentType;
import com.swissotel.swissotel360.reservations.interfaces.rest.resources.*;

public final class ReservationRestMapper {
	private ReservationRestMapper() {
	}

	public static CreateReservationCommand toCommand(CreateReservationResource r) {
		return new CreateReservationCommand(r.guestPublicId(), r.roomTypePublicId(), r.checkInDate(), r.checkOutDate(),
				PaymentType.fromCode(r.paymentType()));
	}

	public static ModifyReservationCommand toCommand(java.util.UUID id, ModifyReservationResource r) {
		return new ModifyReservationCommand(id, r.roomTypePublicId(), r.checkInDate(), r.checkOutDate());
	}

	public static ReservationResource toResource(ReservationResult r) {
		var a = r.reservation();
		var payment = r.prepayment() == null ? null
				: new ReservationResource.Prepayment(r.prepayment().status(), r.prepayment().message());
		return new ReservationResource(a.id().value(), a.code().value(), a.status().code(),
				new ReservationResource.Guest(r.guest().publicId(), r.guest().fullName()),
				new ReservationResource.RoomType(r.roomType().publicId(), r.roomType().name()), a.period().checkIn(),
				a.period().checkOut(), a.period().nights(), a.paymentType().code(), a.totalAmount().amount(),
				r.quote().breakdown().stream()
						.map(n -> new ReservationResource.NightPrice(n.date(), n.price().amount())).toList(),
				r.holdExpiresAt(), a.createdAt(), payment);
	}

	public static ReservationSummaryResource toSummary(ReservationSummary s) {
		var r = s.reservation();
		return new ReservationSummaryResource(r.id().value(), r.code().value(), r.status().code(), s.guestPublicId(),
				s.guestName(), s.roomTypePublicId(), s.roomTypeName(), r.period().checkIn(), r.period().checkOut(),
				r.paymentType().code(), r.totalAmount().amount(), r.createdAt());
	}
}

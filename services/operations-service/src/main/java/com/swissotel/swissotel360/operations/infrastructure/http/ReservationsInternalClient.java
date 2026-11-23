package com.swissotel.swissotel360.operations.infrastructure.http;

import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayReservationSnapshot;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface ReservationsInternalClient {

	@GetExchange("/internal/reservations/by-code/{code}")
	StayReservationSnapshot findByCode(@PathVariable("code") String code);

	@PostExchange("/internal/reservations/{reservationId}/complete")
	void markCompleted(@PathVariable("reservationId") Long reservationId);
}

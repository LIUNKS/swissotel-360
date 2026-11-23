package com.swissotel.swissotel360.rooms.infrastructure.http;

import com.swissotel.swissotel360.rooms.application.internal.outboundservices.acl.ReservationsAvailabilityService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReservationsHttpAvailabilityAdapter implements ReservationsAvailabilityService {

	private final ReservationsInternalClient client;

	public ReservationsHttpAvailabilityAdapter(ReservationsInternalClient client) {
		this.client = client;
	}

	@Override
	public long countOverlappingActiveReservations(Long id, LocalDate checkIn, LocalDate checkOut) {
		var response = client.countOverlapping(id, checkIn, checkOut);
		return response == null ? 0L : response.count();
	}

	@Override
	public boolean hasReservations(Long id) {
		var response = client.hasReservations(id);
		return response != null && response.hasReservations();
	}
}

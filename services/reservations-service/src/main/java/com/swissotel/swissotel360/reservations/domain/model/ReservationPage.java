package com.swissotel.swissotel360.reservations.domain.model;

import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import java.util.List;

public record ReservationPage(List<Reservation> content, long totalElements) {
	public ReservationPage {
		content = List.copyOf(content);
	}
}

package com.swissotel.swissotel360.reservations.application.dto;

import java.util.List;

public record ReservationSummaryPage(List<ReservationSummary> content, long totalElements, int page, int size) {
	public ReservationSummaryPage {
		content = List.copyOf(content);
	}
}

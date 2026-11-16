package com.swissotel.swissotel360.rooms.domain.model.queries;

import java.time.LocalDate;

public record AvailabilitySearch(LocalDate checkIn, LocalDate checkOut, Integer guests) {
	public AvailabilitySearch {
		validatePeriod(checkIn, checkOut);
		if (guests != null && guests <= 0)
			throw new IllegalArgumentException("guests must be positive");
	}

	static void validatePeriod(LocalDate checkIn, LocalDate checkOut) {
		if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn))
			throw new IllegalArgumentException("checkOut must be after checkIn");
		if (checkIn.isBefore(LocalDate.now()))
			throw new IllegalArgumentException("checkIn cannot be in the past");
	}
}

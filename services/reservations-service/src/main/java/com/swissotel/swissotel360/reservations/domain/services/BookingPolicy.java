package com.swissotel.swissotel360.reservations.domain.services;

import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidStayPeriodException;
import com.swissotel.swissotel360.reservations.domain.model.StayPeriod;
import java.time.LocalDate;

public final class BookingPolicy {
	private final int maxNights;

	public BookingPolicy(int maxNights) {
		if (maxNights < 1)
			throw new IllegalArgumentException("maxNights must be positive");
		this.maxNights = maxNights;
	}

	public void validate(StayPeriod period, LocalDate today) {
		if (period.checkIn().isBefore(today))
			throw new InvalidStayPeriodException("check_in_in_the_past");
		if (period.nights() > maxNights)
			throw new InvalidStayPeriodException("exceeds_max_nights");
	}
}

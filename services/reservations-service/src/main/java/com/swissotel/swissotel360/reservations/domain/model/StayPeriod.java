package com.swissotel.swissotel360.reservations.domain.model;

import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidStayPeriodException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public record StayPeriod(LocalDate checkIn, LocalDate checkOut) {
	public StayPeriod {
		if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
			throw new InvalidStayPeriodException("check_out_must_be_after_check_in");
		}
	}

	public int nights() {
		return Math.toIntExact(ChronoUnit.DAYS.between(checkIn, checkOut));
	}

	public boolean overlaps(StayPeriod other) {
		return checkIn.isBefore(other.checkOut) && checkOut.isAfter(other.checkIn);
	}

	public Stream<LocalDate> eachNight() {
		return checkIn.datesUntil(checkOut);
	}
}

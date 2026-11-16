package com.swissotel.swissotel360.reservations.domain;

import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidStayPeriodException;
import com.swissotel.swissotel360.reservations.domain.exceptions.NoAvailabilityException;
import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;
import com.swissotel.swissotel360.reservations.domain.model.StayPeriod;
import com.swissotel.swissotel360.reservations.domain.services.AvailabilityPolicy;
import com.swissotel.swissotel360.reservations.domain.services.BookingPolicy;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ReservationPolicyTest {
	private final LocalDate today = LocalDate.parse("2026-10-27");

	@Test
	void bookingPolicyRejectsPastDatesAndExcessiveStays() {
		var policy = new BookingPolicy(30);
		assertEquals("check_in_in_the_past",
				assertThrows(InvalidStayPeriodException.class,
						() -> policy.validate(new StayPeriod(today.minusDays(1), today.plusDays(1)), today))
						.getReason());
		assertEquals("exceeds_max_nights", assertThrows(InvalidStayPeriodException.class,
				() -> policy.validate(new StayPeriod(today, today.plusDays(31)), today)).getReason());
		assertDoesNotThrow(() -> policy.validate(new StayPeriod(today, today.plusDays(30)), today));
	}

	@Test
	void availabilityRequiresAtLeastOneUnreservedSellableUnit() {
		var policy = new AvailabilityPolicy();
		var type = new RoomTypeId(1L);
		assertDoesNotThrow(() -> policy.assertAvailable(3, 2, type));
		assertThrows(NoAvailabilityException.class, () -> policy.assertAvailable(2, 2, type));
		assertThrows(NoAvailabilityException.class, () -> policy.assertAvailable(0, 0, type));
	}
}

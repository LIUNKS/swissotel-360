package com.swissotel.swissotel360.reservations.domain;

import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidStayPeriodException;
import com.swissotel.swissotel360.reservations.domain.model.StayPeriod;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class StayPeriodTest {
    @Test void validatesCheckoutAndCountsNights() {
        var period=new StayPeriod(LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-13"));
        assertEquals(3,period.nights());
        assertEquals(3,period.eachNight().count());
        assertThrows(InvalidStayPeriodException.class,()->new StayPeriod(period.checkIn(),period.checkIn()));
    }
    @Test void usesHalfOpenRangesForOverlap() {
        var first=new StayPeriod(LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-13"));
        assertFalse(first.overlaps(new StayPeriod(LocalDate.parse("2026-11-13"),LocalDate.parse("2026-11-15"))));
        assertTrue(first.overlaps(new StayPeriod(LocalDate.parse("2026-11-12"),LocalDate.parse("2026-11-14"))));
    }
}

package com.swissotel.swissotel360.stays.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.events.StayCheckedIn;
import com.swissotel.swissotel360.stays.domain.events.StayCheckedOut;
import com.swissotel.swissotel360.stays.domain.exceptions.StayAlreadyClosedException;
import com.swissotel.swissotel360.stays.domain.model.KeyStatus;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class StayAggregateTest {
	private static final Instant IN = Instant.parse("2026-10-27T15:00:00Z");
	private static final Instant OUT = Instant.parse("2026-10-29T10:00:00Z");

	@Test
	void checkInIssuesActiveKeyAndPublishesEvent() {
		var stay = Stay.checkIn(1L, 2L, 3L, 4L, IN);
		assertEquals(StayStatus.ACTIVE, stay.status());
		assertEquals(12, stay.activeKey().orElseThrow().accessCode().length());
		assertTrue(stay.domainEvents().stream().anyMatch(StayCheckedIn.class::isInstance));
	}

	@Test
	void checkOutClosesStayRevokesKeysAndRejectsSecondCheckOut() {
		var stay = Stay.checkIn(1L, 2L, 3L, 4L, IN);
		stay.checkOut(OUT);
		assertEquals(StayStatus.CLOSED, stay.status());
		assertEquals(OUT, stay.checkedOutAt());
		assertTrue(stay.activeKey().isEmpty());
		assertTrue(stay.keys().stream().allMatch(k -> k.status() == KeyStatus.REVOKED));
		assertTrue(stay.domainEvents().stream().anyMatch(StayCheckedOut.class::isInstance));
		assertThrows(StayAlreadyClosedException.class, () -> stay.checkOut(OUT));
		assertThrows(StayAlreadyClosedException.class, () -> stay.issueKey(OUT));
	}

	@Test
	void issuingKeyRevokesPreviousOne() {
		var stay = Stay.checkIn(1L, 2L, 3L, 4L, IN);
		var first = stay.activeKey().orElseThrow();
		var second = stay.issueKey(OUT);
		assertEquals(KeyStatus.REVOKED, first.status());
		assertSame(second, stay.activeKey().orElseThrow());
	}
}

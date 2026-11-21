package com.swissotel.swissotel360.reservations.domain;

import com.swissotel.swissotel360.reservations.domain.model.CancellationReason;
import com.swissotel.swissotel360.reservations.domain.model.PaymentType;
import com.swissotel.swissotel360.reservations.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReservationEnumsTest {
	@Test
	void reservationStatusFromCodeRejectsInvalidNullAndBlankWithoutClassLeak() {
		var invalid = assertThrows(IllegalArgumentException.class, () -> ReservationStatus.fromCode("zzz"));
		assertEquals("Invalid reservation status: zzz", invalid.getMessage());
		assertFalse(invalid.getMessage().contains("com.swissotel"));

		var nullEx = assertThrows(IllegalArgumentException.class, () -> ReservationStatus.fromCode(null));
		assertFalse(nullEx.getMessage().contains("com.swissotel"));

		var blankEx = assertThrows(IllegalArgumentException.class, () -> ReservationStatus.fromCode("   "));
		assertFalse(blankEx.getMessage().contains("com.swissotel"));

		assertEquals(ReservationStatus.PENDING, ReservationStatus.fromCode("pending"));
		assertEquals(ReservationStatus.CONFIRMED, ReservationStatus.fromCode(" CONFIRMED "));
	}

	@Test
	void cancellationReasonFromCodeRejectsInvalidNullAndBlankWithoutClassLeak() {
		var invalid = assertThrows(IllegalArgumentException.class, () -> CancellationReason.fromCode("zzz"));
		assertEquals("Invalid cancellation reason: zzz", invalid.getMessage());
		assertFalse(invalid.getMessage().contains("com.swissotel"));

		var nullEx = assertThrows(IllegalArgumentException.class, () -> CancellationReason.fromCode(null));
		assertFalse(nullEx.getMessage().contains("com.swissotel"));

		var blankEx = assertThrows(IllegalArgumentException.class, () -> CancellationReason.fromCode("   "));
		assertFalse(blankEx.getMessage().contains("com.swissotel"));

		assertEquals(CancellationReason.GUEST_REQUEST, CancellationReason.fromCode("guest_request"));
		assertEquals(CancellationReason.STAFF, CancellationReason.fromCode("STAFF"));
	}

	@Test
	void paymentTypeFromCodeRejectsInvalidNullAndBlankWithoutClassLeak() {
		var invalid = assertThrows(IllegalArgumentException.class, () -> PaymentType.fromCode("zzz"));
		assertEquals("Invalid payment type: zzz", invalid.getMessage());
		assertFalse(invalid.getMessage().contains("com.swissotel"));

		var nullEx = assertThrows(IllegalArgumentException.class, () -> PaymentType.fromCode(null));
		assertFalse(nullEx.getMessage().contains("com.swissotel"));

		var blankEx = assertThrows(IllegalArgumentException.class, () -> PaymentType.fromCode("   "));
		assertFalse(blankEx.getMessage().contains("com.swissotel"));

		assertEquals(PaymentType.IMMEDIATE, PaymentType.fromCode("immediate"));
		assertEquals(PaymentType.DEFERRED, PaymentType.fromCode(" DEFERRED "));
	}
}

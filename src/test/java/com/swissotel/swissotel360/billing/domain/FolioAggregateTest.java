package com.swissotel.swissotel360.billing.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.exceptions.FolioClosedException;
import com.swissotel.swissotel360.billing.domain.exceptions.PaymentAmountMismatchException;
import com.swissotel.swissotel360.billing.domain.model.FolioStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FolioAggregateTest {
	private static final Instant T0 = Instant.parse("2026-10-27T15:00:00Z");

	@Test
	void addsChargesAndRecalculatesTotal() {
		var folio = Folio.open(1L);
		folio.addCharge("Minibar", new BigDecimal("10.5"), null, T0);
		folio.addCharge("Spa", new BigDecimal("20.00"), 7L, T0);
		assertEquals(new BigDecimal("30.50"), folio.total());
		assertEquals(2, folio.charges().size());
	}

	@Test
	void rejectsPaymentMismatchThenClosesAndRejectsCharges() {
		var folio = Folio.open(1L);
		folio.addCharge("Minibar", new BigDecimal("10.00"), null, T0);
		assertThrows(PaymentAmountMismatchException.class, () -> folio.pay(new BigDecimal("9.99")));
		assertTrue(folio.isOpen());
		folio.pay(new BigDecimal("10.00"));
		assertEquals(FolioStatus.CLOSED, folio.status());
		assertThrows(FolioClosedException.class, () -> folio.addCharge("Late", BigDecimal.ONE, null, T0));
	}
}

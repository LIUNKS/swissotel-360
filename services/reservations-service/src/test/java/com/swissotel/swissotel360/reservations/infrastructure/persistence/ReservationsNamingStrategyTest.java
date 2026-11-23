package com.swissotel.swissotel360.reservations.infrastructure.persistence;

import com.swissotel.swissotel360.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationsNamingStrategyTest {

	private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy = new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

	@Test
	void mapsReservationsTableAndColumnsToReservationsSqlSchema() {
		assertEquals("reservations",
				strategy.toPhysicalTableName(Identifier.toIdentifier("reservations"), null).getText());
		assertEquals("public_id", strategy.toPhysicalColumnName(Identifier.toIdentifier("public_id"), null).getText());
		assertEquals("reservation_code",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("reservation_code"), null).getText());
		assertEquals("cancellation_reason",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("cancellation_reason"), null).getText());
	}
}

package com.swissotel.swissotel360.billing.infrastructure.persistence;

import com.swissotel.swissotel360.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BillingNamingStrategyTest {

	private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy = new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

	@Test
	void mapsAllBillingTableAndColumnNamesToBillingSqlSchema() {
		for (var tableName : List.of(
				"folios",
				"charges",
				"payment_methods",
				"payments",
				"invoices",
				"processed_integration_events")) {
			assertEquals(tableName,
					strategy.toPhysicalTableName(Identifier.toIdentifier(tableName), null).getText());
		}

		assertEquals("public_id", strategy.toPhysicalColumnName(Identifier.toIdentifier("public_id"), null).getText());
		assertEquals("event_key", strategy.toPhysicalColumnName(Identifier.toIdentifier("event_key"), null).getText());
		assertEquals("event_type", strategy.toPhysicalColumnName(Identifier.toIdentifier("event_type"), null).getText());
		assertEquals("processed_at",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("processed_at"), null).getText());
	}
}

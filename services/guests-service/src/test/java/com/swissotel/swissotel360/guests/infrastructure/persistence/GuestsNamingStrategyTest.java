package com.swissotel.swissotel360.guests.infrastructure.persistence;

import com.swissotel.swissotel360.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuestsNamingStrategyTest {

	private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy = new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

	@Test
	void mapsGuestsTablesAndColumnsToGuestsSqlSchema() {
		for (var tableName : List.of("document_types", "guests")) {
			assertEquals(tableName, strategy.toPhysicalTableName(Identifier.toIdentifier(tableName), null).getText());
		}
		assertEquals("public_id", strategy.toPhysicalColumnName(Identifier.toIdentifier("public_id"), null).getText());
		assertEquals("document_type_id",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("document_type_id"), null).getText());
		assertEquals("document_number",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("document_number"), null).getText());
	}
}

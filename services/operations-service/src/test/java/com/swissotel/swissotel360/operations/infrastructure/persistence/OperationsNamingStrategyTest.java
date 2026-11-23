package com.swissotel.swissotel360.operations.infrastructure.persistence;

import com.swissotel.swissotel360.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsNamingStrategyTest {

	private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy = new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

	@Test
	void mapsOperationsTablesAndColumnsToOperationsSqlSchema() {
		for (var tableName : List.of("stays", "digital_keys", "service_types", "service_requests")) {
			assertEquals(tableName, strategy.toPhysicalTableName(Identifier.toIdentifier(tableName), null).getText());
		}
		assertEquals("public_id", strategy.toPhysicalColumnName(Identifier.toIdentifier("public_id"), null).getText());
		assertEquals("checked_in_at",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("checked_in_at"), null).getText());
		assertEquals("service_type_id",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("service_type_id"), null).getText());
	}
}

package com.swissotel.swissotel360.iam.infrastructure.persistence;

import com.swissotel.swissotel360.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IamNamingStrategyTest {

	private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy = new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

	@Test
	void mapsIamTablesAndColumnsToIamSqlSchema() {
		for (var tableName : List.of("roles", "users")) {
			assertEquals(tableName, strategy.toPhysicalTableName(Identifier.toIdentifier(tableName), null).getText());
		}
		assertEquals("public_id", strategy.toPhysicalColumnName(Identifier.toIdentifier("public_id"), null).getText());
		assertEquals("full_name", strategy.toPhysicalColumnName(Identifier.toIdentifier("full_name"), null).getText());
		assertEquals("last_login_at",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("last_login_at"), null).getText());
	}
}

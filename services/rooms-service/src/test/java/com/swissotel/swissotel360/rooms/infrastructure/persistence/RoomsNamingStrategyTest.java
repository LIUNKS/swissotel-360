package com.swissotel.swissotel360.rooms.infrastructure.persistence;

import com.swissotel.swissotel360.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomsNamingStrategyTest {

	private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy = new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

	@Test
	void mapsRoomsTablesAndColumnsToRoomsSqlSchema() {
		for (var tableName : List.of("room_types", "rooms", "rates")) {
			assertEquals(tableName, strategy.toPhysicalTableName(Identifier.toIdentifier(tableName), null).getText());
		}
		assertEquals("public_id", strategy.toPhysicalColumnName(Identifier.toIdentifier("public_id"), null).getText());
		assertEquals("room_type_id",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("room_type_id"), null).getText());
		assertEquals("valid_from",
				strategy.toPhysicalColumnName(Identifier.toIdentifier("valid_from"), null).getText());
	}
}

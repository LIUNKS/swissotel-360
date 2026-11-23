package com.swissotel.swissotel360.rooms.domain;

import com.swissotel.swissotel360.rooms.domain.exceptions.InvalidRoomStatusTransitionException;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Room;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoomAggregateTest {

	@Test
	void releasesOccupiedRoomToAvailableAndIsIdempotentWhenAlreadyAvailable() {
		var occupied = new Room(10L, UUID.randomUUID(), 2L, "301", (short) 3, RoomStatus.OCCUPIED);
		var released = occupied.releaseFromCheckIn();
		assertEquals(RoomStatus.AVAILABLE, released.status());
		assertEquals(RoomStatus.AVAILABLE, released.releaseFromCheckIn().status());

		var cleaning = new Room(10L, UUID.randomUUID(), 2L, "301", (short) 3, RoomStatus.CLEANING);
		assertThrows(InvalidRoomStatusTransitionException.class, cleaning::releaseFromCheckIn);
	}
}

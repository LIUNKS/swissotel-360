package com.swissotel.swissotel360.rooms.interfaces.rest;

import com.swissotel.swissotel360.rooms.interfaces.acl.AvailabilityContextFacade;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomAvailabilitySnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomInfoSnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomStatusSnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomTypeInventorySnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomsContextFacade;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoomInternalControllerTest {

	private final RoomsContextFacade rooms = mock(RoomsContextFacade.class);
	private final AvailabilityContextFacade availability = mock(AvailabilityContextFacade.class);
	private final RoomInternalController roomController = new RoomInternalController(rooms);
	private final RoomTypeInternalController roomTypeController = new RoomTypeInternalController(availability);

	@Test
	void exposesRoomAndRoomTypeInternalEndpoints() {
		var roomPublicId = UUID.randomUUID();
		var roomTypePublicId = UUID.randomUUID();
		var roomInfo = new RoomInfoSnapshot(5L, roomPublicId, 2L, "301", "AVAILABLE");
		var typeInfo = new RoomTypeInventorySnapshot(2L, roomTypePublicId, "Deluxe", (short) 3);

		when(rooms.findById(5L)).thenReturn(Optional.of(roomInfo));
		when(rooms.findByPublicId(roomPublicId)).thenReturn(Optional.of(roomInfo));
		when(rooms.findFirstAvailable(2L)).thenReturn(Optional.of(roomInfo));
		when(rooms.markOccupied(5L)).thenReturn(new RoomStatusSnapshot(roomPublicId, "OCCUPIED"));
		when(rooms.markCheckedOut(5L)).thenReturn(new RoomStatusSnapshot(roomPublicId, "CLEANING"));
		when(rooms.releaseRoom(5L)).thenReturn(new RoomStatusSnapshot(roomPublicId, "AVAILABLE"));
		when(rooms.findById(999L)).thenReturn(Optional.empty());

		assertEquals(roomInfo, roomController.getById(5L).getBody());
		assertEquals(roomInfo, roomController.getByPublicId(roomPublicId).getBody());
		assertEquals(roomInfo, roomController.findFirstAvailable(2L).getBody());
		assertEquals("OCCUPIED", roomController.markOccupied(5L).getBody().status());
		assertEquals("CLEANING", roomController.markCheckedOut(5L).getBody().status());
		assertEquals("AVAILABLE", roomController.releaseRoom(5L).getBody().status());
		assertEquals(HttpStatus.NOT_FOUND, roomController.getById(999L).getStatusCode());

		var in = LocalDate.of(2026, 11, 22);
		var out = LocalDate.of(2026, 11, 24);
		var quote = new RoomAvailabilitySnapshot(roomTypePublicId, in, out, 2, new BigDecimal("760.00"),
				List.of(new RoomAvailabilitySnapshot.NightPrice(in, new BigDecimal("380.00"))));
		when(availability.findRoomTypeById(2L)).thenReturn(Optional.of(typeInfo));
		when(availability.findRoomType(roomTypePublicId)).thenReturn(Optional.of(typeInfo));
		when(availability.sellableUnits(2L)).thenReturn(8);
		when(availability.quote(2L, in, out)).thenReturn(quote);

		assertEquals(typeInfo, roomTypeController.getById(2L).getBody());
		assertEquals(typeInfo, roomTypeController.getByPublicId(roomTypePublicId).getBody());
		var units = roomTypeController.sellableUnits(2L).getBody();
		assertNotNull(units);
		assertEquals(8, units.sellableUnits());
		assertEquals(quote, roomTypeController.quote(2L, in, out).getBody());
	}
}

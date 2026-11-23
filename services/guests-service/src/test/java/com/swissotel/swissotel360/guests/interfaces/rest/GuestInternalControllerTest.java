package com.swissotel.swissotel360.guests.interfaces.rest;

import com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectoryContextFacade;
import com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectorySnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GuestInternalControllerTest {

	private final GuestDirectoryContextFacade guests = mock(GuestDirectoryContextFacade.class);
	private final GuestInternalController controller = new GuestInternalController(guests);

	@Test
	void returnsGuestSnapshotByIdPublicIdAndUserIdOr404() {
		var publicId = UUID.randomUUID();
		var snapshot = new GuestDirectorySnapshot(11L, publicId, 5L, "Ana Garcia", "ana@swissotel.com", "DNI",
				"12345678");

		when(guests.findById(11L)).thenReturn(Optional.of(snapshot));
		when(guests.findByPublicId(publicId)).thenReturn(Optional.of(snapshot));
		when(guests.findByUserId(5L)).thenReturn(Optional.of(snapshot));
		when(guests.findById(999L)).thenReturn(Optional.empty());

		assertEquals(HttpStatus.OK, controller.getById(11L).getStatusCode());
		assertEquals(snapshot, controller.getById(11L).getBody());
		assertEquals(snapshot, controller.getByPublicId(publicId).getBody());
		assertEquals(snapshot, controller.getByUserId(5L).getBody());
		assertEquals(HttpStatus.NOT_FOUND, controller.getById(999L).getStatusCode());
		assertEquals(HttpStatus.NOT_FOUND, controller.getByPublicId(UUID.randomUUID()).getStatusCode());
		assertEquals(HttpStatus.NOT_FOUND, controller.getByUserId(999L).getStatusCode());
	}
}

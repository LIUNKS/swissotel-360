package com.swissotel.swissotel360.stays.interfaces.rest;

import com.swissotel.swissotel360.stays.interfaces.acl.StayContextSnapshot;
import com.swissotel.swissotel360.stays.interfaces.acl.StaysContextFacade;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StayInternalControllerTest {

	private final StaysContextFacade stays = mock(StaysContextFacade.class);
	private final StayInternalController controller = new StayInternalController(stays);

	@Test
	void returnsSnapshotByInternalIdAndByPublicIdOr404WhenMissing() {
		var publicId = UUID.randomUUID();
		var snapshot = new StayContextSnapshot(42L, publicId, 10L, 20L, 30L, "active",
				Instant.parse("2026-11-22T15:00:00Z"), null);

		when(stays.findById(42L)).thenReturn(Optional.of(snapshot));
		when(stays.findByPublicId(publicId)).thenReturn(Optional.of(snapshot));
		when(stays.findById(999L)).thenReturn(Optional.empty());
		when(stays.findByPublicId(UUID.fromString("00000000-0000-0000-0000-000000000001")))
				.thenReturn(Optional.empty());

		var byId = controller.getById(42L);
		assertEquals(HttpStatus.OK, byId.getStatusCode());
		assertEquals(snapshot, byId.getBody());

		var byPublicId = controller.getByPublicId(publicId);
		assertEquals(HttpStatus.OK, byPublicId.getStatusCode());
		assertEquals(snapshot, byPublicId.getBody());

		assertEquals(HttpStatus.NOT_FOUND, controller.getById(999L).getStatusCode());
		assertEquals(HttpStatus.NOT_FOUND,
				controller.getByPublicId(UUID.fromString("00000000-0000-0000-0000-000000000001")).getStatusCode());
	}
}

package com.swissotel.swissotel360.reservations.interfaces.rest;

import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationContextSnapshot;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationsContextFacade;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationInternalControllerTest {

	private final ReservationsContextFacade facade = mock(ReservationsContextFacade.class);
	private final ReservationInternalController controller = new ReservationInternalController(facade);

	@Test
	void exposesAvailabilityByCodeAndLifecycleActions() {
		var resPublicId = UUID.randomUUID();
		var in = LocalDate.of(2026, 11, 22);
		var out = LocalDate.of(2026, 11, 24);
		var snapshot = new ReservationContextSnapshot(resPublicId, "SW26-ABC12345", 10L, 2L, in, out, "confirmed",
				42L);

		when(facade.countActiveReservationsOverlapping(2L, in, out)).thenReturn(4L);
		when(facade.hasReservationsForRoomType(2L)).thenReturn(true);
		when(facade.findByCode("SW26-ABC12345")).thenReturn(Optional.of(snapshot));
		when(facade.findByCode("MISSING")).thenReturn(Optional.empty());

		var overlapping = controller.countOverlapping(2L, in, out).getBody();
		assertNotNull(overlapping);
		assertEquals(4L, overlapping.count());

		var hasRes = controller.hasReservations(2L).getBody();
		assertNotNull(hasRes);
		assertTrue(hasRes.hasReservations());

		assertEquals(snapshot, controller.findByCode("SW26-ABC12345").getBody());
		assertEquals(HttpStatus.NOT_FOUND, controller.findByCode("MISSING").getStatusCode());

		assertEquals(HttpStatus.NO_CONTENT, controller.markCompleted(42L).getStatusCode());
		verify(facade).markCompleted(42L);

		assertEquals(HttpStatus.NO_CONTENT, controller.confirmPrepaid(42L).getStatusCode());
		verify(facade).confirmPrepaid(42L);
	}
}

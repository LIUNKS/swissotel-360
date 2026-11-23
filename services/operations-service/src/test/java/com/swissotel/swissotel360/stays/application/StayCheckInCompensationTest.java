package com.swissotel.swissotel360.stays.application;

import com.swissotel.swissotel360.stays.application.command.CheckInCommand;
import com.swissotel.swissotel360.stays.application.internal.commandservices.StayCommandServiceImpl;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayCollaboratorsOutboundService;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayGuestSnapshot;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayReservationSnapshot;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayRoomSnapshot;
import com.swissotel.swissotel360.stays.application.port.StayEventPublisher;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.repositories.StayRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StayCheckInCompensationTest {

	private static final Instant NOW = Instant.parse("2026-11-22T15:00:00Z");
	private final StayRepository stays = mock(StayRepository.class);
	private final StayCollaboratorsOutboundService collaborators = mock(StayCollaboratorsOutboundService.class);
	private final StayEventPublisher events = mock(StayEventPublisher.class);
	private final StayCommandServiceImpl service = new StayCommandServiceImpl(
			stays, collaborators, events, Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void releasesOccupiedRoomWhenReservationCompletionFails() {
		var roomUuid = UUID.randomUUID();
		var reservation = new StayReservationSnapshot(UUID.randomUUID(), "SW26-COMP1", 20L, 2L,
				LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 24), "confirmed", 100L);
		var guest = new StayGuestSnapshot(20L, UUID.randomUUID(), null, "Ada Lovelace", "ada@swissotel.com",
				"DNI", "12345678");
		var room = new StayRoomSnapshot(30L, roomUuid, 2L, "301", "AVAILABLE");

		when(collaborators.findReservation("SW26-COMP1")).thenReturn(Optional.of(reservation));
		when(stays.existsByReservationId(100L)).thenReturn(false);
		when(collaborators.findGuest(20L)).thenReturn(Optional.of(guest));
		when(collaborators.findRoom(roomUuid)).thenReturn(Optional.of(room));
		when(collaborators.currentUserId()).thenReturn(1L);
		when(stays.save(any(Stay.class))).thenAnswer(inv -> inv.getArgument(0));
		doThrow(new IllegalStateException("reservations-service unreachable"))
				.when(collaborators).completeReservation(100L);

		var ex = assertThrows(IllegalStateException.class,
				() -> service.checkIn(new CheckInCommand("SW26-COMP1", "DNI", "12345678", roomUuid)));

		assertEquals("reservations-service unreachable", ex.getMessage());
		verify(collaborators).markRoomOccupied(30L);
		verify(collaborators).releaseRoom(30L);
		verify(events, never()).publishAfterCommit(any(), any());
	}

	@Test
	void attachesSuppressedExceptionWhenCompensationAlsoFails() {
		var roomUuid = UUID.randomUUID();
		var reservation = new StayReservationSnapshot(UUID.randomUUID(), "SW26-COMP2", 20L, 2L,
				LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 24), "confirmed", 101L);
		var guest = new StayGuestSnapshot(20L, UUID.randomUUID(), null, "Ada Lovelace", "ada@swissotel.com",
				"DNI", "12345678");
		var room = new StayRoomSnapshot(31L, roomUuid, 2L, "302", "AVAILABLE");

		when(collaborators.findReservation("SW26-COMP2")).thenReturn(Optional.of(reservation));
		when(stays.existsByReservationId(101L)).thenReturn(false);
		when(collaborators.findGuest(20L)).thenReturn(Optional.of(guest));
		when(collaborators.findRoom(roomUuid)).thenReturn(Optional.of(room));
		when(collaborators.currentUserId()).thenReturn(1L);
		when(stays.save(any(Stay.class))).thenAnswer(inv -> inv.getArgument(0));
		doThrow(new IllegalStateException("complete failed")).when(collaborators).completeReservation(101L);
		doThrow(new IllegalStateException("release failed")).when(collaborators).releaseRoom(31L);

		var ex = assertThrows(IllegalStateException.class,
				() -> service.checkIn(new CheckInCommand("SW26-COMP2", "DNI", "12345678", roomUuid)));

		assertEquals("complete failed", ex.getMessage());
		assertEquals(1, ex.getSuppressed().length);
		assertEquals("release failed", ex.getSuppressed()[0].getMessage());
	}
}

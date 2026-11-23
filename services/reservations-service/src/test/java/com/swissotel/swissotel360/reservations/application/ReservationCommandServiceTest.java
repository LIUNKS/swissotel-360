package com.swissotel.swissotel360.reservations.application;

import com.swissotel.swissotel360.reservations.application.command.CreateReservationCommand;
import com.swissotel.swissotel360.reservations.application.internal.commandservices.ReservationCommandServiceImpl;
import com.swissotel.swissotel360.reservations.application.policy.ReservationAccessPolicy;
import com.swissotel.swissotel360.reservations.application.port.*;
import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort.CurrentActor;
import com.swissotel.swissotel360.reservations.application.port.GuestDirectoryPort.GuestSnapshot;
import com.swissotel.swissotel360.reservations.application.port.RoomInventoryPort.*;
import com.swissotel.swissotel360.reservations.domain.exceptions.GuestProfileRequiredException;
import com.swissotel.swissotel360.reservations.domain.exceptions.NoAvailabilityException;
import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationRepository;
import com.swissotel.swissotel360.reservations.domain.services.*;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReservationCommandServiceTest {
	private final CurrentActorPort actors = mock(CurrentActorPort.class);
	private final GuestDirectoryPort guests = mock(GuestDirectoryPort.class);
	private final RoomInventoryPort rooms = mock(RoomInventoryPort.class);
	private final PrepaymentPort prepayments = mock(PrepaymentPort.class);
	private final ReservationRepository reservations = mock(ReservationRepository.class);
	private final ReservationCodeGenerator codes = mock(ReservationCodeGenerator.class);
	private final ReservationEventPublisher events = mock(ReservationEventPublisher.class);
	private final Clock clock = Clock.fixed(Instant.parse("2026-10-27T18:30:00Z"), ZoneOffset.UTC);
	private final UUID roomTypePublicId = UUID.randomUUID();
	private final GuestSnapshot guest = new GuestSnapshot(new GuestId(2L), UUID.randomUUID(), "Ada Guest",
			"ada@example.test");
	private final RoomTypeSnapshot roomType = new RoomTypeSnapshot(new RoomTypeId(3L), roomTypePublicId, "Deluxe",
			(short) 2);
	private ReservationCommandServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new ReservationCommandServiceImpl(actors, guests, rooms, prepayments, reservations, codes,
				new BookingPolicy(30),
				new AvailabilityPolicy(), events, new ReservationAccessPolicy(), clock, Duration.ofMinutes(15));
		when(rooms.findRoomType(roomTypePublicId)).thenReturn(Optional.of(roomType));
		when(rooms.sellableUnits(roomType.id())).thenReturn(3);
		when(rooms.quote(eq(roomType.id()), any()))
				.thenReturn(new PriceQuote(new Money(new BigDecimal("1140.00")), List.of()));
		when(reservations.countActiveOverlapping(eq(roomType.id()), any(), any())).thenReturn(1L);
		when(reservations.existsByCode(any())).thenReturn(false);
		when(reservations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(codes.next()).thenReturn(new ReservationCode("SW26-7H3KQ9XA"));
	}

	@Test
	void createsDeferredStaffBookingAsConfirmed() {
		when(actors.current())
				.thenReturn(new CurrentActor(new UserId(9L), "ROLE_RECEPTIONIST", Set.of("reservations.create")));
		when(guests.findByPublicId(any())).thenReturn(Optional.of(guest));
		var result = service.create(command(guest.publicId(), PaymentType.DEFERRED));
		assertEquals(ReservationStatus.CONFIRMED, result.reservation().status());
		assertEquals(guest.id(), result.reservation().guestId());
		assertNull(result.holdExpiresAt());
		assertNull(result.prepayment());
		verify(reservations).lockRoomType(roomType.id());
		verify(events).publishAfterCommit(anyCollection());
	}

	@Test
	void guestSelfServiceIgnoresAnotherGuestIdAndRequestsPrepayment() {
		when(actors.current())
				.thenReturn(new CurrentActor(new UserId(9L), "ROLE_GUEST", Set.of("reservations.create")));
		when(guests.findByUserId(new UserId(9L))).thenReturn(Optional.of(guest));
		when(prepayments.request(any(), any(), any())).thenReturn(PrepaymentPort.PrepaymentRequest.unavailable());
		var result = service.create(command(UUID.randomUUID(), PaymentType.IMMEDIATE));
		assertEquals(ReservationStatus.PENDING, result.reservation().status());
		assertEquals(guest.id(), result.reservation().guestId());
		assertEquals(Instant.now(clock).plus(Duration.ofMinutes(15)), result.holdExpiresAt());
		assertEquals("not_configured", result.prepayment().status());
		verify(guests, never()).findByPublicId(any());
	}

	@Test
	void rejectsGuestWithoutProfileAndFullInventory() {
		when(actors.current()).thenReturn(new CurrentActor(new UserId(9L), "ROLE_GUEST", Set.of()));
		when(guests.findByUserId(new UserId(9L))).thenReturn(Optional.empty());
		assertThrows(GuestProfileRequiredException.class, () -> service.create(command(null, PaymentType.DEFERRED)));

		when(actors.current()).thenReturn(new CurrentActor(new UserId(8L), "ROLE_RECEPTIONIST", Set.of()));
		when(guests.findByPublicId(any())).thenReturn(Optional.of(guest));
		when(rooms.sellableUnits(roomType.id())).thenReturn(1);
		when(reservations.countActiveOverlapping(eq(roomType.id()), any(), any())).thenReturn(1L);
		assertThrows(NoAvailabilityException.class,
				() -> service.create(command(guest.publicId(), PaymentType.DEFERRED)));
		verify(rooms, never()).quote(any(), any());
	}

	private CreateReservationCommand command(UUID guestId, PaymentType payment) {
		return new CreateReservationCommand(guestId, roomTypePublicId, LocalDate.parse("2026-11-10"),
				LocalDate.parse("2026-11-13"), payment);
	}
}

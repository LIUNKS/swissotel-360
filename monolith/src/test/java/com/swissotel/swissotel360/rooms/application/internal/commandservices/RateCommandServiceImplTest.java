package com.swissotel.swissotel360.rooms.application.internal.commandservices;

import com.swissotel.swissotel360.rooms.domain.exceptions.RateOverlapException;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Rate;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.RoomType;
import com.swissotel.swissotel360.rooms.domain.model.commands.CreateRateCommand;
import com.swissotel.swissotel360.rooms.domain.repositories.RateRepository;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateCommandServiceImplTest {
	private final RateRepository rates = mock(RateRepository.class);
	private final RoomTypeRepository roomTypes = mock(RoomTypeRepository.class);
	private final RateCommandServiceImpl service = new RateCommandServiceImpl(rates, roomTypes);
	private final UUID roomTypePublicId = UUID.randomUUID();
	private final RoomType roomType = new RoomType(5L, roomTypePublicId, "Executive Suite", (short) 2,
			"Spacious suite");

	@Test
	void createsRateWithValidTo() {
		var from = LocalDate.of(2026, 6, 1);
		var to = LocalDate.of(2026, 8, 31);
		when(roomTypes.findByPublicId(roomTypePublicId)).thenReturn(Optional.of(roomType));
		when(rates.findOverlapping(roomType.id(), from, to)).thenReturn(List.of());
		when(rates.save(any())).thenAnswer(invocation -> {
			Rate rate = invocation.getArgument(0);
			return new Rate(100L, rate.publicId(), rate.roomTypeId(), rate.roomTypePublicId(), rate.price(),
					rate.season(), rate.validFrom(), rate.validTo());
		});

		var created = service.create(
				new CreateRateCommand(roomTypePublicId, new BigDecimal("420.00"), "high-season", from, to));

		assertEquals(100L, created.id());
		assertNotNull(created.publicId());
		assertEquals(roomType.id(), created.roomTypeId());
		assertEquals(roomTypePublicId, created.roomTypePublicId());
		assertEquals(new BigDecimal("420.00"), created.price());
		assertEquals("high-season", created.season());
		assertEquals(from, created.validFrom());
		assertEquals(to, created.validTo());
		verify(rates).findOverlapping(roomType.id(), from, to);
		verify(rates).save(any());
	}

	@Test
	void createsOpenEndedRateWithoutValidTo() {
		var from = LocalDate.of(2026, 9, 1);
		when(roomTypes.findByPublicId(roomTypePublicId)).thenReturn(Optional.of(roomType));
		when(rates.findOverlapping(roomType.id(), from, null)).thenReturn(List.of());
		when(rates.save(any())).thenAnswer(invocation -> {
			Rate rate = invocation.getArgument(0);
			return new Rate(101L, rate.publicId(), rate.roomTypeId(), rate.roomTypePublicId(), rate.price(),
					rate.season(), rate.validFrom(), rate.validTo());
		});

		var created = service
				.create(new CreateRateCommand(roomTypePublicId, new BigDecimal("300.00"), null, from, null));

		assertEquals(101L, created.id());
		assertNotNull(created.publicId());
		assertEquals(roomType.id(), created.roomTypeId());
		assertEquals(roomTypePublicId, created.roomTypePublicId());
		assertEquals(new BigDecimal("300.00"), created.price());
		assertNull(created.season());
		assertEquals(from, created.validFrom());
		assertNull(created.validTo());
		verify(rates).findOverlapping(roomType.id(), from, null);
		verify(rates).save(any());
	}

	@Test
	void rejectsOverlappingRate() {
		var from = LocalDate.of(2026, 6, 1);
		var to = LocalDate.of(2026, 6, 30);
		var existing = new Rate(1L, UUID.randomUUID(), roomType.id(), roomTypePublicId, new BigDecimal("300.00"),
				"base", LocalDate.of(2026, 6, 15), null);
		when(roomTypes.findByPublicId(roomTypePublicId)).thenReturn(Optional.of(roomType));
		when(rates.findOverlapping(roomType.id(), from, to)).thenReturn(List.of(existing));

		var exception = assertThrows(RateOverlapException.class, () -> service.create(
				new CreateRateCommand(roomTypePublicId, new BigDecimal("420.00"), "high-season", from, to)));

		assertEquals(List.of(LocalDate.of(2026, 6, 15)), exception.getConflicts());
		verify(rates, never()).save(any());
	}

	@Test
	void rejectsRateWhenValidToIsBeforeValidFrom() {
		var from = LocalDate.of(2026, 6, 10);
		var to = LocalDate.of(2026, 6, 9);

		assertThrows(IllegalArgumentException.class, () -> service.create(
				new CreateRateCommand(roomTypePublicId, new BigDecimal("420.00"), "high-season", from, to)));

		verifyNoInteractions(roomTypes, rates);
	}
}

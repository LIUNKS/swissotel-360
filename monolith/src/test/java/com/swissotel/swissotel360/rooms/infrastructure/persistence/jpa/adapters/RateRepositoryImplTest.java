package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.RatePersistenceEntity;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.RoomTypePersistenceEntity;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories.RatePersistenceRepository;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories.RoomTypePersistenceRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateRepositoryImplTest {
	private final RatePersistenceRepository jpaRepository = mock(RatePersistenceRepository.class);
	private final RoomTypePersistenceRepository roomTypeJpaRepository = mock(RoomTypePersistenceRepository.class);
	private final RateRepositoryImpl repository = new RateRepositoryImpl(jpaRepository, roomTypeJpaRepository);

	@Test
	void findOverlappingDispatchesToOpenEndedWhenToIsNull() {
		var roomTypePublicId = UUID.randomUUID();
		var ratePublicId = UUID.randomUUID();
		var roomTypeEntity = new RoomTypePersistenceEntity(1L, roomTypePublicId, "Deluxe", (short) 2, "Deluxe room");
		var from = LocalDate.of(2026, 6, 1);
		var entity = new RatePersistenceEntity(10L, ratePublicId, roomTypeEntity, new BigDecimal("420.00"),
				"high-season", from, null);
		when(jpaRepository.findOverlappingOpenEnded(1L, from)).thenReturn(List.of(entity));

		var result = repository.findOverlapping(1L, from, null);

		assertEquals(1, result.size());
		assertEquals(10L, result.get(0).id());
		assertEquals(ratePublicId, result.get(0).publicId());
		assertEquals(1L, result.get(0).roomTypeId());
		assertEquals(roomTypePublicId, result.get(0).roomTypePublicId());
		assertEquals(new BigDecimal("420.00"), result.get(0).price());
		assertEquals("high-season", result.get(0).season());
		assertEquals(from, result.get(0).validFrom());
		assertNull(result.get(0).validTo());
		verify(jpaRepository).findOverlappingOpenEnded(1L, from);
		verify(jpaRepository, never()).findOverlappingClosed(any(), any(), any());
		verifyNoMoreInteractions(jpaRepository);
	}

	@Test
	void findOverlappingDispatchesToClosedWhenToIsPresent() {
		var roomTypePublicId = UUID.randomUUID();
		var ratePublicId = UUID.randomUUID();
		var roomTypeEntity = new RoomTypePersistenceEntity(1L, roomTypePublicId, "Deluxe", (short) 2, "Deluxe room");
		var from = LocalDate.of(2026, 6, 1);
		var to = LocalDate.of(2026, 8, 31);
		var entity = new RatePersistenceEntity(11L, ratePublicId, roomTypeEntity, new BigDecimal("350.00"), "regular",
				from, to);
		when(jpaRepository.findOverlappingClosed(1L, from, to)).thenReturn(List.of(entity));

		var result = repository.findOverlapping(1L, from, to);

		assertEquals(1, result.size());
		assertEquals(11L, result.get(0).id());
		assertEquals(ratePublicId, result.get(0).publicId());
		assertEquals(1L, result.get(0).roomTypeId());
		assertEquals(roomTypePublicId, result.get(0).roomTypePublicId());
		assertEquals(new BigDecimal("350.00"), result.get(0).price());
		assertEquals("regular", result.get(0).season());
		assertEquals(from, result.get(0).validFrom());
		assertEquals(to, result.get(0).validTo());
		verify(jpaRepository).findOverlappingClosed(1L, from, to);
		verify(jpaRepository, never()).findOverlappingOpenEnded(any(), any());
		verifyNoMoreInteractions(jpaRepository);
	}
}

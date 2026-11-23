package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.rooms.domain.model.queries.RoomFilter;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.RoomPersistenceEntity;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.RoomTypePersistenceEntity;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories.RoomPersistenceRepository;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories.RoomTypePersistenceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoomRepositoryImplTest {
	private final RoomPersistenceRepository jpaRepository = mock(RoomPersistenceRepository.class);
	private final RoomTypePersistenceRepository roomTypeJpaRepository = mock(RoomTypePersistenceRepository.class);
	private final RoomRepositoryImpl repository = new RoomRepositoryImpl(jpaRepository, roomTypeJpaRepository);

	@Test
	void findUsesFindAllWhenBothFiltersAreNull() {
		var pageable = PageRequest.of(0, 20);
		var entity = roomEntity(3L, RoomStatus.AVAILABLE);
		when(jpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

		var result = repository.find(new RoomFilter(null, null, 0, 20));

		assertEquals(1, result.totalElements());
		assertEquals(1, result.content().size());
		assertEquals("301", result.content().get(0).roomNumber());
		verify(jpaRepository).findAll(pageable);
		verify(jpaRepository, never()).findByRoomType_Id(any(), any());
		verify(jpaRepository, never()).findByStatus(any(), any());
		verify(jpaRepository, never()).findByRoomType_IdAndStatus(any(), any(), any());
		verifyNoMoreInteractions(jpaRepository);
	}

	@Test
	void findUsesFindByRoomTypeIdWhenOnlyRoomTypeIdIsPresent() {
		var pageable = PageRequest.of(1, 10);
		var entity = roomEntity(3L, RoomStatus.CLEANING);
		when(jpaRepository.findByRoomType_Id(3L, pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 11));

		var result = repository.find(new RoomFilter(3L, null, 1, 10));

		assertEquals(11, result.totalElements());
		assertEquals(1, result.content().size());
		assertEquals(3L, result.content().get(0).roomTypeId());
		assertEquals(RoomStatus.CLEANING, result.content().get(0).status());
		verify(jpaRepository).findByRoomType_Id(3L, pageable);
		verify(jpaRepository, never()).findAll(any(Pageable.class));
		verify(jpaRepository, never()).findByStatus(any(), any());
		verify(jpaRepository, never()).findByRoomType_IdAndStatus(any(), any(), any());
		verifyNoMoreInteractions(jpaRepository);
	}

	@Test
	void findUsesFindByStatusWhenOnlyStatusIsPresent() {
		var pageable = PageRequest.of(0, 5);
		var entity = roomEntity(4L, RoomStatus.OCCUPIED);
		when(jpaRepository.findByStatus(RoomStatus.OCCUPIED, pageable))
				.thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

		var result = repository.find(new RoomFilter(null, RoomStatus.OCCUPIED, 0, 5));

		assertEquals(1, result.totalElements());
		assertEquals(1, result.content().size());
		assertEquals(RoomStatus.OCCUPIED, result.content().get(0).status());
		verify(jpaRepository).findByStatus(RoomStatus.OCCUPIED, pageable);
		verify(jpaRepository, never()).findAll(any(Pageable.class));
		verify(jpaRepository, never()).findByRoomType_Id(any(), any());
		verify(jpaRepository, never()).findByRoomType_IdAndStatus(any(), any(), any());
		verifyNoMoreInteractions(jpaRepository);
	}

	@Test
	void findUsesFindByRoomTypeIdAndStatusWhenBothFiltersArePresent() {
		var pageable = PageRequest.of(0, 20);
		var entity = roomEntity(3L, RoomStatus.OUT_OF_SERVICE);
		when(jpaRepository.findByRoomType_IdAndStatus(3L, RoomStatus.OUT_OF_SERVICE, pageable))
				.thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

		var result = repository.find(new RoomFilter(3L, RoomStatus.OUT_OF_SERVICE, 0, 20));

		assertEquals(1, result.totalElements());
		assertEquals(1, result.content().size());
		assertEquals(3L, result.content().get(0).roomTypeId());
		assertEquals(RoomStatus.OUT_OF_SERVICE, result.content().get(0).status());
		assertEquals(entity.getPublicId(), result.content().get(0).publicId());
		verify(jpaRepository).findByRoomType_IdAndStatus(3L, RoomStatus.OUT_OF_SERVICE, pageable);
		verify(jpaRepository, never()).findAll(any(Pageable.class));
		verify(jpaRepository, never()).findByRoomType_Id(any(), any());
		verify(jpaRepository, never()).findByStatus(any(), any());
		verifyNoMoreInteractions(jpaRepository);
	}

	private RoomPersistenceEntity roomEntity(Long roomTypeId, RoomStatus status) {
		var typeEntity = new RoomTypePersistenceEntity(roomTypeId, UUID.randomUUID(), "Suite", (short) 2, "Luxury");
		return new RoomPersistenceEntity(10L, UUID.randomUUID(), typeEntity, "301", (short) 3, status);
	}
}

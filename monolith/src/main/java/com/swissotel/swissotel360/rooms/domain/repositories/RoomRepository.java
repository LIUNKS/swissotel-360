package com.swissotel.swissotel360.rooms.domain.repositories;

import com.swissotel.swissotel360.rooms.domain.model.aggregates.Room;
import com.swissotel.swissotel360.rooms.domain.model.queries.RoomFilter;
import com.swissotel.swissotel360.rooms.domain.model.queries.RoomPage;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository {
	Room save(Room room);

	Optional<Room> findByPublicId(UUID publicId);

	Optional<Room> findById(Long id);

	boolean existsByRoomNumber(String roomNumber);

	long countByRoomTypeId(Long roomTypeId);

	long countByRoomTypeIdAndStatusNot(Long roomTypeId,
			com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus status);

	RoomPage find(RoomFilter filter);

	void delete(Room room);
}

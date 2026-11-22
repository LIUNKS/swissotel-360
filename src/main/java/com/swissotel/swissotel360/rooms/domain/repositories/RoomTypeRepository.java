package com.swissotel.swissotel360.rooms.domain.repositories;

import com.swissotel.swissotel360.rooms.domain.model.aggregates.RoomType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomTypeRepository {
	RoomType save(RoomType roomType);

	Optional<RoomType> findByPublicId(UUID publicId);

	Optional<RoomType> findById(Long id);

	List<RoomType> findAll();

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	boolean existsById(Long id);

	void delete(RoomType roomType);
}

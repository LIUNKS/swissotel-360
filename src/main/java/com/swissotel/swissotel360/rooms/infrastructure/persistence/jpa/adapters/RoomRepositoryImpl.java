package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.rooms.domain.exceptions.RoomInUseException;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Room;
import com.swissotel.swissotel360.rooms.domain.model.queries.RoomFilter;
import com.swissotel.swissotel360.rooms.domain.model.queries.RoomPage;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomRepository;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.assemblers.RoomsPersistenceAssembler;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories.RoomPersistenceRepository;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories.RoomTypePersistenceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RoomRepositoryImpl implements RoomRepository {
	private final RoomPersistenceRepository repository;
	private final RoomTypePersistenceRepository roomTypes;

	public RoomRepositoryImpl(RoomPersistenceRepository repository, RoomTypePersistenceRepository roomTypes) {
		this.repository = repository;
		this.roomTypes = roomTypes;
	}

	public Room save(Room room) {
		var type = roomTypes.findById(room.roomTypeId()).orElseThrow();
		try {
			return RoomsPersistenceAssembler
					.toDomain(repository.saveAndFlush(RoomsPersistenceAssembler.toEntity(room, type)));
		} catch (DataIntegrityViolationException ex) {
			throw new com.swissotel.swissotel360.rooms.domain.exceptions.DuplicateRoomNumberException(
					room.roomNumber());
		}
	}

	public Optional<Room> findByPublicId(UUID id) {
		return repository.findByPublicId(id).map(RoomsPersistenceAssembler::toDomain);
	}

	public Optional<Room> findById(Long id) {
		return repository.findById(id).map(RoomsPersistenceAssembler::toDomain);
	}

	public boolean existsByRoomNumber(String number) {
		return repository.existsByRoomNumber(number);
	}

	public long countByRoomTypeId(Long id) {
		return repository.countByRoomType_Id(id);
	}

	public long countByRoomTypeIdAndStatusNot(Long id, RoomStatus status) {
		return repository.countByRoomType_IdAndStatusNot(id, status);
	}

	public RoomPage find(RoomFilter filter) {
		var page = repository.findFiltered(filter.roomTypeId(), filter.status(),
				PageRequest.of(filter.page(), filter.size()));
		return new RoomPage(page.getContent().stream().map(RoomsPersistenceAssembler::toDomain).toList(),
				page.getTotalElements());
	}

	public void delete(Room room) {
		try {
			repository.deleteById(room.id());
		} catch (DataIntegrityViolationException ex) {
			throw new RoomInUseException(room.publicId());
		}
	}
}

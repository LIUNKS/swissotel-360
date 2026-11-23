package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.RoomPersistenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RoomPersistenceRepository extends JpaRepository<RoomPersistenceEntity, Long> {
	Optional<RoomPersistenceEntity> findByPublicId(UUID publicId);

	boolean existsByRoomNumber(String number);

	long countByRoomType_Id(Long roomTypeId);

	long countByRoomType_IdAndStatusNot(Long roomTypeId, RoomStatus status);

	Page<RoomPersistenceEntity> findByRoomType_Id(Long roomTypeId, Pageable pageable);

	Page<RoomPersistenceEntity> findByStatus(RoomStatus status, Pageable pageable);

	Page<RoomPersistenceEntity> findByRoomType_IdAndStatus(Long roomTypeId, RoomStatus status, Pageable pageable);
}

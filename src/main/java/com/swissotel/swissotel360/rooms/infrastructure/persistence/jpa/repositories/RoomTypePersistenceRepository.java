package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.RoomTypePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface RoomTypePersistenceRepository extends JpaRepository<RoomTypePersistenceEntity,Long> {
    Optional<RoomTypePersistenceEntity> findByPublicId(UUID publicId);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name,Long id);
}

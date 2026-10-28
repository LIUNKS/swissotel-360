package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.RoomTypePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
public interface RoomTypePersistenceRepository extends JpaRepository<RoomTypePersistenceEntity,Long> {
    Optional<RoomTypePersistenceEntity> findByPublicId(UUID publicId);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name,Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RoomTypePersistenceEntity t where t.id = :id")
    Optional<RoomTypePersistenceEntity> lockInventoryById(@Param("id") Long id);
}

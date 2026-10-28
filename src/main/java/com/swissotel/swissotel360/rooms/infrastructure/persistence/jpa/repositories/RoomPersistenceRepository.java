package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.RoomPersistenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
public interface RoomPersistenceRepository extends JpaRepository<RoomPersistenceEntity,Long> {
    Optional<RoomPersistenceEntity> findByPublicId(UUID publicId);
    boolean existsByRoomNumber(String number);
    long countByRoomType_Id(Long roomTypeId);
    long countByRoomType_IdAndStatusNot(Long roomTypeId,RoomStatus status);
    @Query("select r from RoomPersistenceEntity r where (:typeId is null or r.roomType.id=:typeId) and (:status is null or r.status=:status)") Page<RoomPersistenceEntity> findFiltered(@Param("typeId") Long typeId,@Param("status") RoomStatus status,Pageable pageable);
}

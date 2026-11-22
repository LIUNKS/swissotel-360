package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.entities.RatePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatePersistenceRepository extends JpaRepository<RatePersistenceEntity, Long> {
	Optional<RatePersistenceEntity> findByPublicId(UUID publicId);

	List<RatePersistenceEntity> findByRoomType_IdOrderByValidFromDesc(Long roomTypeId);

	boolean existsByRoomType_Id(Long roomTypeId);

	@Query("select r from RatePersistenceEntity r where r.roomType.id=:typeId and r.validFrom<=:date and (r.validTo is null or r.validTo>=:date)")
	Optional<RatePersistenceEntity> findEffective(@Param("typeId") Long typeId, @Param("date") LocalDate date);

	@Query("select r from RatePersistenceEntity r where r.roomType.id = :typeId and (r.validTo is null or r.validTo >= :from)")
	List<RatePersistenceEntity> findOverlappingOpenEnded(@Param("typeId") Long typeId, @Param("from") LocalDate from);

	@Query("select r from RatePersistenceEntity r where r.roomType.id = :typeId and r.validFrom <= :to and (r.validTo is null or r.validTo >= :from)")
	List<RatePersistenceEntity> findOverlappingClosed(@Param("typeId") Long typeId, @Param("from") LocalDate from,
			@Param("to") LocalDate to);
}
